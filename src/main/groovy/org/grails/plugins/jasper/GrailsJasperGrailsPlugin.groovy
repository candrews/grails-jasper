package org.grails.plugins.jasper

import grails.plugins.Plugin
import net.sf.jasperreports.j2ee.servlets.ImageServlet
import org.springframework.boot.context.embedded.ServletRegistrationBean

class GrailsJasperGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.2 > *"

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            'docs/**',
            'src/docs/**'
    ]

    // TODO Fill in these fields
    def title = "Jasper Plugin" // Headline display name of the plugin
    def author = "Craig Andrews"
    def authorEmail = "candrews@integralblue.com"
    def description = '''
    Adds easy support for launching jasper reports from GSP pages.
    After installing, run your application and request (app-url)/jasper/demo for a demonstration and instructions.
    '''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://www.grails.org/plugin/jasper"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "Apache License 2.0"

    // Location of the plugin's issue tracker.
    def issueManagement = [system: "JIRA", url: "http://jira.grails.org/browse/GPJASPER"]

    // Online location of the plugin's browseable source code.
    def scm = [url: "https://github.com/candrews/grails-jasper"]

    Closure doWithSpring() {
        { ->
            imageServlet(ImageServlet)
            dispatchServletRegistrationBean(ServletRegistrationBean) {
                servlet = ref(imageServlet)
                urlMappings = ["/reports/image"]
            }
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
