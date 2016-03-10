/* Copyright 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

 package org.grails.plugins.jasper

import groovy.sql.Sql
import net.sf.jasperreports.engine.*
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter
import net.sf.jasperreports.engine.export.JRTextExporterParameter
import net.sf.jasperreports.engine.export.JRXlsExporterParameter
import net.sf.jasperreports.engine.util.JRProperties
import org.springframework.core.io.Resource

import java.lang.reflect.Field
import java.sql.Connection

/**
 * Generates Jasper reports. Call one of the three generateReport methods to
 * get a ByteArrayOutputStream with the generated report.
 * @author Sebastian Hohns
 */
class JasperService {

    def dataSource

    static final boolean FORCE_TEMP_FOLDER = false

    /**
     * Build a JasperReportDef form a parameter map. This is used by the taglib.
     * @param parameters
     * @param locale
     * @param testModel
     * @return reportDef
     */
    JasperReportDef buildReportDefinition(parameters, locale, testModel) {
        JasperReportDef reportDef = new JasperReportDef(name: parameters._file, parameters: parameters,locale: locale)

        reportDef.fileFormat = JasperExportFormat.determineFileFormat(parameters._format)
        reportDef.reportData = getReportData(testModel, parameters)
        reportDef.contentStream = generateReport(reportDef)
        reportDef.jasperPrinter = generatePrinter(reportDef)

        return reportDef
    }

    private Collection getReportData(testModel, parameters) {
        Collection reportData

        if (testModel?.data) {
            try {
                reportData = testModel.data
            } catch (Throwable e) {
                throw new Exception("Expected chainModel.data parameter to be a Collection, but it was ${chainModel.data.class.name}", e)
            }
        } else {
            testModel = getProperties().containsKey('model') ? model : null
            if (testModel?.data) {
                try {
                    reportData = testModel.data
                } catch (Throwable e) {
                    throw new Exception("Expected model.data parameter to be a Collection, but it was ${model.data.class.name}", e)
                }
            } else if (parameters?.data) {
                try {
                    reportData = parameters.data
                } catch (Throwable e) {
                    throw new Exception("Expected data parameter to be a Collection, but it was ${parameters.data.class.name}", e)
                }
            }
        }

        return reportData
    }

    @Deprecated
    ByteArrayOutputStream generateReport(String jasperReportDir, JasperExportFormat format, Collection reportData, Map parameters) {
        JasperReportDef reportDef = new JasperReportDef(name: parameters._file, folder: jasperReportDir, reportData: reportData, fileFormat: format, parameters: parameters)
        return generateReport(reportDef)
    }

    /**
     * Generate a report based on a single jasper file.
     * @param format , target format
     * @param reportDef , jasper report object
     * return ByteArrayOutStreamByteArrayOutStream with the generated Report
     */
    ByteArrayOutputStream generateReport(JasperReportDef reportDef) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream()
        JRExporter exporter = generateExporter(reportDef)

        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArray)
        exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8")

        def jasperPrint = reportDef.jasperPrinter
        if (jasperPrint==null) {
            reportDef.jasperPrinter = generatePrinter(reportDef)
        }

        exporter.setParameter(JRExporterParameter.JASPER_PRINT, reportDef.jasperPrinter)
        exporter.exportReport()

        return byteArray
    }

    /**
     * Generate a single report based on a list of jasper files.
     * @param format , target format
     * @param reports , a List with report objects
     * @param parameters , additional parameters
     * return ByteArrayOutStream with the generated Report
     */
    ByteArrayOutputStream generateReport(List<JasperReportDef> reports) {
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream()
        JRExporter exporter = generateExporter(reports.first())

        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, byteArray)
        exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, "UTF-8")

        def printers = reports.collect { report -> generatePrinter(report) }
        exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, printers)

        exporter.exportReport()

        return byteArray
    }

    /**
     * Forces the Jasper Reports  temp folder to be "~/.grails/.jasper" and ensures that such a folder exists.
     * The user (however the app server is logged in) is much more likely to have read/write/delete rights here than the
     * default location that Jasper Reports uses.
     */
    protected void forceTempFolder() {
        /* TODO This is currently disabled, because it doesn't work. Jasper Reports seems to always use the current
         * folder (.) no matter what.  (I'll be filing a bug report against Jasper Reports itself shortly - Craig Jones 16-Aug-2008)
         */
        if (FORCE_TEMP_FOLDER) {
            // Look up the home folder explicitly (don't trust that tilde notation will work).
            String userHomeDir = System.getProperty('user.home')
            File tempFolder = new File(userHomeDir, "/.grails/.jasper")

            // This is the current official means for setting the temp folder for jasper reports to use when compiling
            // reports on the fly, but it doesn't work
            JRProperties.setProperty(JRProperties.COMPILER_TEMP_DIR, tempFolder.getAbsolutePath())

            // This is a deprecated means for setting the temp folder that supposedly still works (still in the Jasper
            // Reports source code trunk as of 14-Aug-2008, and, in fact, takes precedence over the official method);
            // however, it doesn't work either.
            System.setProperty("jasper.reports.compile.temp", tempFolder.getAbsolutePath())

            if (!tempFolder.exists()) {
                def ant = new AntBuilder()
                ant.mkdir(dir: tempFolder.getAbsolutePath())
                if (!tempFolder.exists()) {
                    throw new Exception("Unable to create temp folder: ${tempFolder.getPath()}")
                }
            }
        }
    }

    /**
     * Generate a exporter with for a JasperReportDef. Note that SUBREPORT_DIR an locale have default
     * values.
     * @param reportDef
     * @return JRExporter
     */
    private JRExporter generateExporter(JasperReportDef reportDef) {
        if (reportDef.parameters.SUBREPORT_DIR == null) {
            reportDef.parameters.SUBREPORT_DIR = reportDef.getFilePath()
        }

        if (reportDef.parameters.locale) {
            if (reportDef.parameters.locale instanceof String) {
                reportDef.parameters.REPORT_LOCALE = getLocaleFromString(reportDef.parameters.locale)
            } else if (reportDef.parameters.locale instanceof Locale) {
                reportDef.parameters.REPORT_LOCALE = reportDef.parameters.locale
            }
        } else if (reportDef.locale) {
            reportDef.parameters.REPORT_LOCALE = reportDef.locale
        } else {
            reportDef.parameters.REPORT_LOCALE = Locale.getDefault()
        }

        JRExporter exporter = JasperExportFormat.getExporter(reportDef.fileFormat)
        Field[] fields = JasperExportFormat.getExporterFields(reportDef.fileFormat)

        Boolean useDefaultParameters = reportDef.parameters.useDefaultParameters.equals("true")
        if (useDefaultParameters) {
            applyDefaultParameters(exporter, reportDef.fileFormat)
        }

        if (fields) {
            applyCustomParameters(fields, exporter, reportDef.parameters)
        }

        return exporter
    }

    /**
     * Generate a JasperPrint object for a given report.
     * @param reportDefinition , the report
     * @param parameters , additional parameters
     * @return JasperPrint , jasperreport printer
     */
    private JasperPrint generatePrinter(JasperReportDef reportDef) {
        JasperPrint jasperPrint
        Resource resource = reportDef.getReport()
        JRDataSource jrDataSource = reportDef.dataSource

        if (jrDataSource == null && reportDef.reportData != null && !reportDef.reportData.isEmpty()) {
            jrDataSource = new JRBeanCollectionDataSource(reportDef.reportData)
        }

        if (jrDataSource != null) {
            if (resource.getFilename().endsWith('.jasper')) {
                jasperPrint = JasperFillManager.fillReport(resource.inputStream, reportDef.parameters, jrDataSource)
            }
            else {
                forceTempFolder()
                jasperPrint = JasperFillManager.fillReport(JasperCompileManager.compileReport(resource.inputStream), reportDef.parameters, jrDataSource)
            }
        }
        else {

            Sql sql = new Sql(dataSource)
            Connection connection = dataSource?.getConnection()

            try {
                if (resource.getFilename().endsWith('.jasper')) {
                    jasperPrint = JasperFillManager.fillReport(resource.inputStream, reportDef.parameters, connection)
                }
                else {
                    forceTempFolder()
                    jasperPrint = JasperFillManager.fillReport(JasperCompileManager.compileReport(resource.inputStream), reportDef.parameters,  connection)
                }
            }
            finally {
                sql.close()
                connection.close()
            }
        }

        return jasperPrint
    }

    /**
     * Apply additional parameters to the exporter. If the user submits a parameter that is not available for
     * the file format this parameter is ignored.
     * @param fields , available fields for the choosen file format
     * @param exporter , the exporter object
     * @param parameter , the parameters to apply
     */
    private void applyCustomParameters(Field[] fields, JRExporter exporter, Map<String, Object> parameters) {
        def fieldNames = fields.collect {it.getName()}

        parameters.each { p ->
            if (fieldNames.contains(p.getKey())) {
                def fld = Class.forName(fields.find {it.name = p.getKey()}.clazz.name).getField(p.getKey())
                exporter.setParameter(fld.get(fld.root.class), p.getValue())
            }
        }
    }

    /**
     * Apply the default parameters for a bunch of file format and only if useDefaultParameters is enabled.
     * @param exporter , the JRExporter
     * @param format , the target file format
     */
    private void applyDefaultParameters(JRExporter exporter, JasperExportFormat format) {
        switch (format) {
            case JasperExportFormat.HTML_FORMAT:
            exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false)
            break
            case JasperExportFormat.XLS_FORMAT:
            exporter.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, true)
            exporter.setParameter(JRXlsExporterParameter.IS_AUTO_DETECT_CELL_TYPE, true)
            exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, false)
            exporter.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, true)
            break
            case JasperExportFormat.TEXT_FORMAT:
            exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, 80)
            exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, 60)
            exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, 60)
            break
        }
    }

    /**
     * Convert a String to a Locale.
     * @param localeString , a string
     * @returns Locale
     */
    static Locale getLocaleFromString(String localeString) {
        if (localeString == null) {
            return null
        }
        localeString = localeString.trim()

        // Extract language
        int languageIndex = localeString.indexOf('_')
        String language
        if (languageIndex == -1) {  // No further "_" so is "{language}" only
            return new Locale(localeString, "")
        }
        language = localeString.substring(0, languageIndex)

        // Extract country
        int countryIndex = localeString.indexOf('_', languageIndex + 1)
        String country
        if (countryIndex == -1) {     // No further "_" so is "{language}_{country}"
            country = localeString.substring(languageIndex + 1)
            return new Locale(language, country)
        }
        // Assume all remaining is the variant so is "{language}_{country}_{variant}"
        country = localeString.substring(languageIndex + 1, countryIndex)
        String variant = localeString.substring(countryIndex + 1)
        return new Locale(language, country, variant)
    }
}
