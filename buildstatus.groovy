import jenkins.*
import jenkins.model.*
import hudson.*
import hudson.model.*
import groovy.text.*
import groovy.text.markup.*
import java.io.*
import jenkins.plugins.build_metrics.*;

import jenkins.plugins.build_metrics.stats.StatsFactory;

import hudson.Extension;
import hudson.Plugin;

import hudson.plugins.global_build_stats.business.GlobalBuildStatsBusiness;
import hudson.plugins.global_build_stats.FieldFilterFactory;
import hudson.plugins.global_build_stats.model.JobBuildSearchResult;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import hudson.EnvVars

EnvVars envVars = build.getEnvironment(listener);

BuildMetricsPluginSearchFactory factory = new BuildMetricsPluginSearchFactory();


BuildMetricsSearch searchCriteria = new BuildMetricsSearch( "Search Results", 1, "Months", "", "", "", "");
StatsFactory buildStats = factory.getBuildStats(searchCriteria);

List<BuildMetricsBuild> failedBuilds = factory.getFailedBuilds(searchCriteria);

def writer = new StringWriter()  // html is written here by markup builder
def markup = new groovy.xml.MarkupBuilder(writer)

markup.html{
  h2("Build Metrics:")
  
  
  table(id: "projectstatus", style:"border: 1px solid #bbb;  border-collapse: collapse;width: 100%;line-height: 1.4em;color: #333;box-sizing: border-box;font-size: 15px;") {
    tr(style:"border: 1px solid black;padding: 3px 4px;box-sizing: border-box;background-color: #A8DBF1;") {
      th( style:"text-align: left;border: 1px solid black;", "Job name")
      th( style:"text-align: left;border: 1px solid black;", "Successful")
      th( style:"text-align: left;border: 1px solid black;", "Failed")
      th( style:"text-align: left;border: 1px solid black;", "Unstable")
      th( style:"text-align: left;border: 1px solid black;", "Aborted")
      th( style:"text-align: left;border: 1px solid black;", "Not Built")
      th( style:"text-align: left;border: 1px solid black;", "Total Builds")
      th( style:"text-align: left;border: 1px solid black;", "Failure Rate")
    }
	
    buildStats.stats.each { stat ->
	  if( "groovy_build_stats" != "${stat.jobName}" ) {
		  tr(style: "border: 1px solid #bbb;padding: 3px 4px;") {
		    td(style: "padding: 3px 4px;vertical-align: middle;border: 1px solid black;color: blue;font-weight: bold;", "${stat.jobName}")
		    td(style: "padding: 3px 4px;vertical-align: middle;border: 1px solid black;color: #0b880b;", "${stat.successes}")
		    td(style: "padding: 3px 4px;vertical-align: middle;border: 1px solid black;color: red;", "${stat.failures}")
		    td(style: "padding: 3px 4px;vertical-align: middle;border: 1px solid black;", "${stat.unstables}")
		    td(style: "padding: 3px 4px;vertical-align: middle;border: 1px solid black;", "${stat.aborts}")
		    td(style: "padding: 3px 4px;vertical-align: middle;border: 1px solid black;", "${stat.noBuilds}")
		    td(style: "padding: 3px 4px;vertical-align: middle;border: 1px solid black;", "${stat.totalBuilds}")
		    td(style: "padding: 3px 4px;vertical-align: middle;border: 1px solid black;", "${stat.failureRate}%")
		  }
        }
     }
    
     tr( style:"border: 1px solid #bbb;padding: 3px 4px;text-align:left;background-color: #A8DBF1;" ) {
          th(style: "font-weight:bold;border: 1px solid black;", "Total:")
          th(style: "font-weight:bold;border: 1px solid black;", "${buildStats.successes}")
          th(style: "font-weight:bold;border: 1px solid black;", "${buildStats.failures}")
          th(style: "font-weight:bold;border: 1px solid black;", "${buildStats.unstables}")
          th(style: "font-weight:bold;border: 1px solid black;", "${buildStats.aborts}")
          th(style: "font-weight:bold;border: 1px solid black;", "${buildStats.noBuilds}")
          th(style: "font-weight:bold;border: 1px solid black;", "${buildStats.totalBuilds}")
          th(style: "font-weight:bold;border: 1px solid black;", "${buildStats.failureRate}%")
        }


  }
  
}
workspace = envVars.get('WORKSPACE')

def build_metric_html = workspace + '/build_metric.html'

File file_build = new File(build_metric_html)
file_build.setText( writer.toString() )
