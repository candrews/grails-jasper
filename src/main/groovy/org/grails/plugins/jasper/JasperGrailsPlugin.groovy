package org.grails.plugins.jasper

import grails.plugins.Plugin
import net.sf.jasperreports.j2ee.servlets.ImageServlet
import org.springframework.boot.context.embedded.ServletRegistrationBean

class JasperGrailsPlugin extends Plugin {

    def author = "Craig Andrews"
    def authorEmail = "candrews@integralblue.com"
    def license = "Apache License 2.0"
    def scm = [url: "https://github.com/mugren/grails-jasper/tree/grails3"]


    def title = "Jasper Plugin"
    def description = '''
	Adds easy support for launching jasper reports from GSP pages.
	After installing, run your application and request (app-url)/jasper/demo for a demonstration and instructions.
    '''
    def documentation = "http://www.grails.org/plugin/jasper"

    Closure doWithSpring() { {->
        //instead doWithWebDescriptor
        image(ServletRegistrationBean,new ImageServlet(),'/reports/image')
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
