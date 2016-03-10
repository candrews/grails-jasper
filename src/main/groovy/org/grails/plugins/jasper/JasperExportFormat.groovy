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

import net.sf.jasperreports.engine.JRExporter
import net.sf.jasperreports.engine.export.*
import net.sf.jasperreports.engine.export.oasis.JROdsExporter
import net.sf.jasperreports.engine.export.oasis.JROdtExporter
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporterParameter
import net.sf.jasperreports.engine.export.ooxml.JRPptxExporter
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter

import java.lang.reflect.Field

/*
 * The supported file formats with their mimetype and file extension.
 * @author Sebastian Hohns
 */

enum JasperExportFormat implements Serializable {
  PDF_FORMAT("application/pdf", "pdf", false),
  HTML_FORMAT("text/html", "html", true),
  XML_FORMAT("text/xml", "xml", false),
  CSV_FORMAT("text/csv", "csv", false),
  XLS_FORMAT("application/vnd.ms-excel", "xls", false),
  RTF_FORMAT("text/rtf", "rtf", false),
  TEXT_FORMAT("text/plain", "txt", true),
  ODT_FORMAT("application/vnd.oasis.opendocument.text", "odt", false),
  ODS_FORMAT("application/vnd.oasis.opendocument.spreadsheetl", "ods", false),
  DOCX_FORMAT("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx", false),
  XLSX_FORMAT("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx", false),
  PPTX_FORMAT("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx", false)

  String mimeTyp
  String extension
  boolean inline

  private JasperExportFormat(String mimeTyp, String extension, boolean inline) {
    this.mimeTyp = mimeTyp
    this.extension = extension
    this.inline = inline
  }

  /**
   * Return the JasperExportFormat for a given format string.
   * @param format as String
   * @return JasperExportFormat
   */
  static JasperExportFormat determineFileFormat(String format) {
    switch (format) {
      case "PDF":  return JasperExportFormat.PDF_FORMAT
      case "HTML": return JasperExportFormat.HTML_FORMAT
      case "XML":  return JasperExportFormat.XML_FORMAT
      case "CSV":  return JasperExportFormat.CSV_FORMAT
      case "XLS":  return JasperExportFormat.XLS_FORMAT
      case "RTF":  return JasperExportFormat.RTF_FORMAT
      case "TEXT": return JasperExportFormat.TEXT_FORMAT
      case "ODT":  return JasperExportFormat.ODT_FORMAT
      case "ODS":  return JasperExportFormat.ODS_FORMAT
      case "DOCX": return JasperExportFormat.DOCX_FORMAT
      case "XLSX": return JasperExportFormat.XLSX_FORMAT
      case "PPTX": return JasperExportFormat.PPTX_FORMAT
      default: throw new Exception(message(code: "jasper.controller.invalidFormat", args: [format]))
    }
  }

  /**
   * Return the suitable Exporter for a given file format.
   * @param format
   * @return exporter
   */
  static JRExporter getExporter(JasperExportFormat format) {
    switch (format) {
      case PDF_FORMAT:  return new JRPdfExporter()
      case HTML_FORMAT: return new JRHtmlExporter()
      case XML_FORMAT:  return new JRXmlExporter()
      case CSV_FORMAT:  return new JRCsvExporter()
      case XLS_FORMAT:  return new JRXlsExporter()
      case RTF_FORMAT:  return new JRRtfExporter()
      case TEXT_FORMAT: return new JRTextExporter()
      case ODT_FORMAT:  return new JROdtExporter()
      case ODS_FORMAT:  return new JROdsExporter()
      case DOCX_FORMAT: return new JRDocxExporter()
      case XLSX_FORMAT: return new JRXlsxExporter()
      case PPTX_FORMAT: return new JRPptxExporter()
      default: throw new Exception(message(code: "jasper.controller.invalidFormat", args: [format]))
    }
  }

  /**
   * Return the available Fields for a given JasperExportFormat.
   * @param format
   * @return Field[] , null if no fields are available for the format
   */
  static Field[] getExporterFields(JasperExportFormat format) {
    switch (format) {
      case PDF_FORMAT:  return JRPdfExporterParameter.getFields()
      case HTML_FORMAT: return JRHtmlExporterParameter.getFields()
      case XML_FORMAT:  return JRXmlExporterParameter.getFields()
      case CSV_FORMAT:  return JRCsvExporterParameter.getFields()
      case XLS_FORMAT:  return JRXlsExporterParameter.getFields()
      case XLSX_FORMAT: return JRXlsExporterParameter.getFields()
      case RTF_FORMAT:  return JRTextExporterParameter.getFields()
      case DOCX_FORMAT: return JRDocxExporterParameter.getFields()
      default: return null
    }
  }
}
