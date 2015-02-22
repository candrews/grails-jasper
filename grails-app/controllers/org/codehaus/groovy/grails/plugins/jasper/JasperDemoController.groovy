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

package org.codehaus.groovy.grails.plugins.jasper

import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.plugins.jasper.demo.ExamplePersonForReport

/*
 * @author mfpereira 2007
 */
class JasperDemoController {

  def admin() {
    // There is nothing to administer, so just show the demo
    redirect(action: 'demo')
  }
  
  def index() {
    // Suggestion for the bug that I opened in Jira (GPJASPER-76) to solve the absent action and error message on JasperDemoController
    redirect(action: 'demo')
  }

  def demo() {
    // This "people" object in this data model is only for displaying the data in a table on the Demo page (demo.gsp) next to the
    // example that invokes the exampleWithData action, below.  It has nothing to do directly with the data that is
    // actually reported.
    [people: makeUpSomeDemoData()]
  }

  def exampleWithData() {
    // This "data" object in this data model is the data that drives this Jasper report (i.e. what appears in the
    // detail band)
    List reportDetails = makeUpSomeDemoData()
    chain(controller: 'jasper', action: 'index', model: [data: reportDetails], params: params)
  }

  def exampleWithDataChart() {
    // This "data" object in this data model is the data that drives this Jasper report (i.e. what appears in the
    // detail band)
    List reportDetails = makeUpSomeDemoData()
    chain(controller: 'jasper', action: 'index', model: [data: reportDetails], params: params)
  }

  protected List makeUpSomeDemoData() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")

    [new ExamplePersonForReport(name: 'Amy', email: 'amy@example.com', birthday: simpleDateFormat.parse("1980-02-14"), points: 2),
     new ExamplePersonForReport(name: 'Brad', email: 'brad@example.com', birthday: simpleDateFormat.parse("1984-05-21"), points: 1),
     new ExamplePersonForReport(name: 'Charlie', email: 'charlie@example.com', birthday: simpleDateFormat.parse("1982-08-10"), points: 4),
     new ExamplePersonForReport(name: 'Diane', email: 'diane@example.com', birthday: simpleDateFormat.parse("1979-04-13"), points: 7),
     new ExamplePersonForReport(name: 'Edward', email: 'edward@example.com', birthday: simpleDateFormat.parse("1985-01-29"), points: 2)]
  }
}
