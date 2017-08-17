import hudson.model.*
import hudson.plugins.pmd.parser.PmdParser;
import hudson.plugins.analysis.core.AbstractAnnotationParser;
import java.io.IOException;
import java.io.InputStream;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import com.google.common.collect.Sets;
import groovy.io.FileType;
import java.io.*;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import hudson.plugins.analysis.util.model.FileAnnotation;
import hudson.plugins.analysis.util.model.MavenModule;
import hudson.plugins.analysis.util.model.Priority;
import hudson.plugins.analysis.util.model.WorkspaceFile;
import hudson.plugins.checkstyle.rules.CheckStyleRules;
import hudson.plugins.checkstyle.parser.Warning;
import org.jfree.chart.ChartUtilities;
import java.awt.Color;

import java.awt.BasicStroke;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.ui.RectangleInsets;
import org.jfree.data.category.CategoryDataset; 
import org.jfree.data.category.DefaultCategoryDataset; 

import org.jfree.chart.axis.CategoryAxis;
import hudson.util.ShiftedCategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;

def config = new HashMap()
def bindings = getBinding()
config.putAll(bindings.getVariables())

def out = config['out']

class ParseFileClass
{
  ParseFileClass(out) {
    def list = []
    def dir = new File("/var/lib/jenkins/reportpmd")

    dir.eachFileRecurse (FileType.FILES) { file ->
      list << file
    }

    def result = list.sort{ a,b -> a.lastModified() <=> b.lastModified() }*.name

    DefaultCategoryDataset dataset = new DefaultCategoryDataset(); 
    def HighMap   = []
    def LowMap    = []
    def NormalMap = []
      
    result.each { it ->

      def inputStream = new File('/var/lib/jenkins/reportpmd',it).newInputStream();
      def Collection<FileAnnotation> annotations = new PmdParser().parse(inputStream, "empty");
      def build_id = it.tokenize( "\\.")[0];
      def module = new MavenModule();

      module.addAnnotations(annotations);

      HighMap[build_id.toInteger()]   = module.getNumberOfHighAnnotations();
      LowMap[build_id.toInteger()]    = module.getNumberOfLowAnnotations();
      NormalMap[build_id.toInteger()] = module.getNumberOfNormalAnnotations();

    }

    LowMap.eachWithIndex{ k, v ->
      if(k != null)
        dataset.addValue(k, "Low", v); 
    }

    NormalMap.eachWithIndex{ k, v ->
      if(k != null)
        dataset.addValue(k, "Normal", v); 
    }

    HighMap.eachWithIndex{ k, v ->
      if(k != null)
        dataset.addValue(k, "High", v);
    }

    JFreeChart xylineChart = ChartFactory.createStackedAreaChart(
      "PMD Analysis",
      "Build Id",
      "Count",
      dataset,
      PlotOrientation.VERTICAL,
      false,
      false,
      false 
    );

    xylineChart.setBackgroundPaint(Color.white);
    def plot = xylineChart.getCategoryPlot();
    double marginval =  0.0;
    double marginpval =  5.0;
    CategoryItemRenderer renderer =  plot.getRenderer(0);
    CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    def insect = new RectangleInsets(marginval, marginval, marginval, marginpval);

    plot.setRangeGridlinesVisible(true);
    plot.setRangeGridlinePaint(Color.black);
    plot.setDomainAxis(domainAxis);
    plot.setBackgroundPaint(Color.WHITE);
    plot.setOutlinePaint(null);
    plot.setForegroundAlpha(0.8f);
    plot.setInsets(insect);
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);    
    domainAxis.setLowerMargin(marginval);
    domainAxis.setUpperMargin(marginval);   
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    xylineChart.getCategoryPlot().getDomainAxis().setCategoryMargin(marginval);
      
    renderer.setSeriesPaint(0, new Color(0x3465a4));
    renderer.setSeriesPaint(1, new Color(0xedd400));
    renderer.setSeriesPaint(2, new Color(0xCC0000));  
    plot.setRenderer(renderer);
      
    int width = 640;    /* Width of the image */
    int height = 480;   /* Height of the image */
    File lineChart = new File( "/var/lib/jenkins/BuildChart.png" );
    ChartUtilities.saveChartAsPNG(lineChart ,xylineChart, width ,height);
  }
}
output = new ParseFileClass(out)
