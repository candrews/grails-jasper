/* Copyright 2006-2009 the original author or authors.
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

import org.grails.taglib.GroovyPageAttributes;

/**
 * @author mfpereira 2007
 */
class JasperTagLib {

    JasperService jasperService

    final static requiredAttrs = ['jasper','format']

    /* The jasperReport tag generates a button for every file specified file format. With a click on one of these icons you generate the report which is returned as the response.
     * Note that this tag should not be nested with a &lt;form&gt; element, as it uses a &lt;form&gt; element in its implementation, and nesting of &lt;form&gt;s is not allowed.
     * 
     * @attr jasper - REQUIRED filepath, relative to your configured report folder, of the report, no file extension needed
     * @attr format - REQUIRED supply the file formats you want to use in a simple list
     * @attr name - OPTIONAL name of the report
     * @attr delimiter - OPTIONAL delimiter between the icons representing the file formats.
     * @attr delimiterBefore - OPTIONAL delimiter in front of the icons
     * @attr delimiterAfter - OPTIONAL delimiter at the end of the icons
     * @attr description - OPTIONAL description of the report
     * @attr buttonPosition - OPTIONAL position of the icons (top or below)
     */
	def jasperReport = { GroovyPageAttributes attrs, body ->
        validateAttributes(attrs)
        String jasperName = attrs['jasper']
        String jasperNameNoPunct = jasperName.replaceAll(/[^a-zA-Z0-9]/, '')
        String idAttr = attrs['id'] ?: ""
        String reportName = attrs['name'] ?: ""
        String delimiter = attrs['delimiter'] ?: "|"
        String delimiterBefore = attrs['delimiterBefore'] ?: delimiter
        String delimiterAfter = attrs['delimiterAfter'] ?: delimiterBefore
        String description = attrs['description'] ?: (reportName ? "<strong>${reportName}</strong>" : "")
        String formClass = attrs['class'] ?: "jasperReport"
        String buttonClass = attrs['buttonClass'] ?: "jasperButton"
        String heightAttr = attrs['height'] ? ' height="' + attrs['height'] + '"' : '' // leading space on purpose
        boolean buttonsBelowBody = (attrs['buttonPosition'] ?: 'top') == 'bottom'

        String controller = attrs['controller'] ?: "jasper"
        String action = attrs['action'] ?: ""

        def bodyContent = body()
        if (bodyContent) {
            // The tag has a body that, presumably, includes input field(s), so we need to wrap it in a form
            out << renderJavascriptForForm(jasperNameNoPunct)
            out << """<form class="${formClass}"${idAttr ? ' id="' + idAttr + '"' : ''} name="${jasperName}" action="${g.createLink(controller: controller, action: action)}">"""
            out << """<input type="hidden" name="_format" />
<input type="hidden" name="_name" value="${reportName}" />
<input type="hidden" name="_file" value="${jasperName}" />
"""

            if (attrs['inline']) {
                out << """<input type="hidden" name="_inline" value="${attrs['inline']}" />\n"""
            }

            if (buttonsBelowBody) {
                out << description << bodyContent << "&nbsp;"
                out << renderButtons(attrs, delimiter, delimiterBefore, delimiterAfter, buttonClass, jasperNameNoPunct,heightAttr)
            } else {
                out << renderButtons(attrs, delimiter, delimiterBefore, delimiterAfter, buttonClass, jasperNameNoPunct,heightAttr)
                out << "&nbsp;" << description << bodyContent
            }

            out << "</form>"
        } else {
            /*
             * The tag has no body, so we don't need a whole form, just a link.
             */
            String result = delimiterBefore
            attrs['format'].toUpperCase().split(",").eachWithIndex {String it, i ->
				it = it.trim()
                if (i > 0) result += delimiter
                result += """ <a class="${buttonClass}" title="${it}" href="${g.createLink(controller: controller, action: action, params: [_format: it, _name: reportName, _file: jasperName])}"> """
                result += asset.image(src: "icons/${it}.gif",alt:it)+"</a>"

            }
            result += delimiterAfter+' '+description
            out << result
        }
    }

    protected String renderJavascriptForForm(String jasperNameNoPunct) {
        // function gets a unique name in case this tag is repeated on the page
        """
      <script type="text/javascript">
        function submit_${jasperNameNoPunct}(link) {
          link.parentNode._format.value = link.title;
          link.parentNode.submit();
          return false;
        }
      </script>
        """
        }

    protected String renderButtons(GroovyPageAttributes attrs, delimiter, String delimiterBefore, String delimiterAfter, String buttonClass, String jasperNameNoPunct, String heightAttr) {
        String result = delimiterBefore
        attrs['format'].toUpperCase().split(",").eachWithIndex {String it, int i ->
            if (i > 0) result += delimiter
            result += """
        <a href="#" class="${buttonClass}" title="${it.trim()}" onclick="return submit_${jasperNameNoPunct}(this)">
        ${asset.image(src: "icons/${it.trim()}.gif",alt:it)}</a>
      """
        }
        result += delimiterAfter
        return result
    }

    private void validateAttributes(GroovyPageAttributes attrs) {
      JasperTagLib.requiredAttrs.each { attrName ->
          if (!attrs[attrName]) {
            // TODO more appropriate Exception type
            throw new Exception(message(code:"jasper.taglib.missingAttribute", args:["${attrName}", "${JasperTagLib.requiredAttrs.join(', ')}"]))
          }
      }
        //Verify the 'format' attribute
        def availableFormats = ["PDF", "HTML", "XML", "CSV", "XLS", "RTF", "TEXT","ODT","ODS","DOCX","XLSX","PPTX"]
        attrs.format.toUpperCase().split(",").each { String it ->
            if (!availableFormats.contains(it.trim())) {
                // TODO more appropriate Exception type
                throw new Exception(message(code: "jasper.taglib.invalidFormatAttribute", args: ["${it}", "${availableFormats}"]))
            }
        }
    }

//Beginning of the new Tags added at plugin version 0.9.5
    /* The jasperForm tag works the same way as the jasperReport tag, but gives you more control over how the form is rendered in HTML.
     * Use the jasperButton tag inside a jasperForm to submit and generate the report.
     * Note that this tag should not be nested with a &lt;form&gt; element, as it uses a &lt;form&gt; element in its implementation, and nesting of &lt;form&gt;s is not allowed.
     * 
     * @attr jasper - REQUIRED filepath, relative to your configured report folder, of the report, no file extension needed.
     * @attr controller - REQUIRED The controller the form will submit to.
     * @attr action - REQUIRED The action the form will submit to.
     * @attr id - OPTIONAL The id attribute for the form.
     * @attr class - OPTIONAL Style class to use for the form. The default is "jasperReport".
     */
    def jasperForm = { GroovyPageAttributes attrs, body ->
        if(!attrs['jasper']) {
            throw new Exception(message(code:"jasper.taglib.missingAttribute", args:'jasper'))
        }

        String jasperName = attrs['jasper']
        String jasperNameNoPunct = jasperName.replaceAll(/[^a-zA-Z0-9]/, '')
        String id = attrs['id'] ?: ""
        String reportName = attrs['name'] ?: ""
        String formClass = attrs['class'] ?: "jasperReport"
        String controller = attrs['controller'] ?: "jasper"
        String action = attrs['action'] ?: ""

        out << """
            <form class="${formClass}" name="${jasperName}" action="${g.createLink(controller: controller, action: action, id: id)}">
            <input type="hidden" name="_format" />
            <input type="hidden" name="_name" value="${reportName}" />
            <input type="hidden" name="_file" value="${jasperName}" />
        """
        request['_jasperFormName'] = jasperNameNoPunct
        out << body()
        request.removeAttribute '_jasperFormName'
        out << renderJavascriptForForm(jasperNameNoPunct)
        out << "</form>"
    }

    //One button for each type of output you would like to make available.
    /* The jasperForm-Tag works the same way as the jasperReport tag, but gives you more control over how the form is rendered in HTML.
     * Use the jasperButton-Tag inside a jasperForm to submit and generate the report.
     * 
     * @attr format - REQUIRED The name of the supported output format. ex. 'pdf' or 'PDF'.
     * @attr class - OPTIONAL Class of the element in addition to default.
     * @attr text - OPTIONAL Text to be included next to button ex. 'print'.
     */
    def jasperButton = { GroovyPageAttributes attrs ->
        if(!attrs['format']){throw new Exception(message(code:"jasper.taglib.missingAttribute", args:'format'))}
        String buttonClass = attrs['class']
        String format = attrs['format'].toUpperCase()
        String text = attrs['text']

        out << """
           <a href="#" class="${format}Button ${buttonClass?:''}" title="${format}" onclick="return submit_${request['_jasperFormName']}(this)" >$text</a>
        """
    }
}
