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

package org.codehaus.groovy.grails.plugins.jasper

import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.JasperPrint

import org.apache.commons.io.FilenameUtils
import grails.util.Holders
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource

/**
 * An abstract representation of a Jasper report.
 * <p>
 * It contains the location of the report design file (name and folder) and
 * data(reportData and/or parameters) needed to fill the report.
 * @author Sebastian Hohns 2010
 */
class JasperReportDef implements Serializable {

  /**
   * The name of the report file without extension.
   * <p>
   * The file can be in jrxml- or jasper-format.
   */
  String name

  /**
   * The parent folder of the report file.
   * <p>
   * This can be an absolute path or an relative path based on class path.
   */
  String folder

  /**
   * The data source used to fill the report.
   * <p>
   * This is a list of java beans.
   */
  Collection reportData

  /**
   * The actual data source used to fill the report.
   * <p>
   * This is an implementation of {@link JRDataSource}.
   */
  JRDataSource dataSource

  /**
   * The target file format.
   */
  JasperExportFormat fileFormat = JasperExportFormat.PDF_FORMAT

  /**
   * The generated report as OutputStream.
   */
  ByteArrayOutputStream contentStream

  /**
   * Additional parameters.
   */
  Map parameters = [:]

  /**
   * Locale setting.
   */
  Locale locale

  JasperPrint jasperPrinter

  private getApplicationContext() {
    return Holders.grailsApplication.mainContext
  }

  /**
   * Looks for the report file in the filesystem. The file extension can either be .jasper
   * or .jrxml.
   * @return the report as Resource
   * @throws Exception , report file not found
   */
  Resource getReport() {
    String path = getFilePath()

    Resource result = getApplicationContext().getResource(path + ".jasper")
    if (result.exists()) {
      return result
    }

    result = new FileSystemResource(path + ".jasper")
    if (result.exists()) {
      return result
    }

    result = getApplicationContext().getResource(path + ".jrxml")
    if (result.exists()) {
      return result
    }

    result = new FileSystemResource(path + ".jrxml")
    if (result.exists()) {
      return result
    }

    throw new Exception("No such report spec: ${path} (jasper or .jrxml)")
  }

  /**
   * Return the file path. The filepath can be set per file (highest priority) or based on
   * the jasper.dir.reports setting. Defaults to /report.
   * @return full path to the report, without extension
   */
  String getFilePath() {
    if (folder) {
      return folder + File.separator + FilenameUtils.getPath(name) + FilenameUtils.getBaseName(name)
    }
    if (Holders.config.jasper.dir.reports) {
      return Holders.config.jasper.dir.reports + File.separator + FilenameUtils.getPath(name) + FilenameUtils.getBaseName(name)
    }
    return "/reports" + File.separator + FilenameUtils.getPath(name) + FilenameUtils.getBaseName(name)
  }

  void setFilePath(String path) {
    folder = FilenameUtils.getPath(path)
    name = FilenameUtils.getName(path)
  }

  /**
   * Add a parameter to the parameter map.
   * @param key , the key
   * @param value , the value
   */
  void addParameter(key, value) {
    parameters.put(key, value)
  }
}
