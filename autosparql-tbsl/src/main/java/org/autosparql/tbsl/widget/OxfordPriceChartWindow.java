package org.autosparql.tbsl.widget;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.autosparql.tbsl.model.Interval;
import org.autosparql.tbsl.util.Intervals;
import org.autosparql.tbsl.util.Labels;
import org.dllearner.utilities.MapUtils;

import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.PointSelectEvent;
import com.invient.vaadin.charts.InvientCharts.Series;
import com.invient.vaadin.charts.InvientCharts.SeriesClickEvent;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig.CategoryAxis;
import com.invient.vaadin.charts.InvientChartsConfig.ColumnConfig;
import com.invient.vaadin.charts.InvientChartsConfig.DataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.HorzAlign;
import com.invient.vaadin.charts.InvientChartsConfig.Legend;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.XAxisDataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class OxfordPriceChartWindow extends Window{
	
	private InvientCharts visibleChart;

	public OxfordPriceChartWindow(String question, String propertyURI, final Map<String, String> uri2Label, final Map<String, Set<Object>> data) {
		setWidth("900px");
		setHeight("900px");
		
		final InvientChartsConfig chartConfig = new InvientChartsConfig();
		final String propertyLabel = Labels.getLabel(propertyURI);
		String title = propertyLabel + " for " + "\"" + question + "\"";
		chartConfig.getTitle().setText(title);
		
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		setContent(mainLayout);
		
		final VerticalLayout diagramHolder = new VerticalLayout();
		diagramHolder.setWidth(null);
		
		CheckBox aggregateCheckBox = new CheckBox("Aggregate");
		aggregateCheckBox.addStyleName("aggregate-box");
		aggregateCheckBox.setHeight(null);
		aggregateCheckBox.setImmediate(true);
		aggregateCheckBox.addListener(new Property.ValueChangeListener() {
			@Override
			public void valueChange(ValueChangeEvent event) {
				diagramHolder.removeComponent(visibleChart);
				visibleChart = getChart(chartConfig, propertyLabel, uri2Label, data, (Boolean)event.getProperty().getValue());
				diagramHolder.addComponent(visibleChart);
				diagramHolder.setExpandRatio(visibleChart, 1f);
			}
		});
		mainLayout.addComponent(aggregateCheckBox);
		mainLayout.setComponentAlignment(aggregateCheckBox, Alignment.MIDDLE_CENTER);
	
//		wnd.setContent(mainLayout);
		
		Panel p = new Panel();
		p.setSizeFull();
		p.setContent(diagramHolder);
		mainLayout.addComponent(p);
		mainLayout.setExpandRatio(p, 1f);
		
		visibleChart = getChart(chartConfig, propertyLabel, uri2Label, data, false);
		diagramHolder.addComponent(visibleChart);
	}
	
	private InvientCharts getChart(InvientChartsConfig chartConfig, String propertyLabel, Map<String, String> uri2Label, Map<String, Set<Object>> data, boolean aggregated){
		 Map<String, Object> sample = Intervals.sample(data);
	        Map<String, Double> castData = new HashMap<String, Double>();
	        for(Entry<String, Object> entry : sample.entrySet()){
	        	castData.put(entry.getKey(), (Double) entry.getValue());
	        }
		InvientCharts chart;
		if(aggregated){
			chart = showAggregatedColumnWithRotatedLabels(chartConfig, propertyLabel, uri2Label, castData);
			chart.setWidth("800px");
		} else {
			chart =  showColumnWithRotatedLabels(chartConfig, propertyLabel, uri2Label, castData);
			int width = 30 * castData.size();
			chart.setWidth(width + "px");
		}
		chart.setHeight("800px");
		
		return chart;
	}
	
	private InvientCharts showColumnWithRotatedLabels(InvientChartsConfig chartConfig, String property, Map<String, String> uri2Label, Map<String, Double> data){
	       
        chartConfig.getGeneralChartConfig().setType(SeriesType.COLUMN);
    
        List<Entry<String, Double>> sortedData = MapUtils.sortByValues(data);
        List<String> categories = new ArrayList<String>();
        String label;
        for(Entry<String, Double> entry : sortedData){
        	String uri = entry.getKey();
        	label = uri2Label.get(uri);
        	if(label == null){
        		label = Labels.getLabelForResource(uri);
        	}
        	categories.add(label);
        }
        
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setCategories(categories);
        xAxis.setLabel(new XAxisDataLabel());
        xAxis.getLabel().setRotation(-45);
        xAxis.getLabel().setAlign(HorzAlign.RIGHT);
        xAxis.getLabel()
                .setStyle("{ font: 'normal 13px Verdana, sans-serif' }");
        LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
        xAxesSet.add(xAxis);
        chartConfig.setXAxes(xAxesSet);

        NumberYAxis yAxis = new NumberYAxis();
        yAxis.setMin(0.0);
        yAxis.setTitle(new AxisTitle("Price in \u00A3"));
        LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
        yAxesSet.add(yAxis);
        chartConfig.setYAxes(yAxesSet);

        chartConfig.setLegend(new Legend(false));

//        chartConfig
//                .getTooltip()
//                .setFormatterJsFunc(
//                        "function() {"
//                                + " return '<b>'+ this.x +'</b><br/>'+ " + property + "' : '+ $wnd.Highcharts.numberFormat(this.y, 1) + "
//                                + " ' millions' " + "}");

        InvientCharts chart = new InvientCharts(chartConfig);

        ColumnConfig colCfg = new ColumnConfig();
        colCfg.setDataLabel(new DataLabel());
        colCfg.getDataLabel().setRotation(-90);
        colCfg.getDataLabel().setAlign(HorzAlign.RIGHT);
        colCfg.getDataLabel().setX(-3);
        colCfg.getDataLabel().setY(10);
        colCfg.getDataLabel().setColor(new RGB(255, 255, 255));
        colCfg.getDataLabel().setFormatterJsFunc(
                "function() {" + " return this.y; " + "}");
        colCfg.getDataLabel().setStyle(
                " { font: 'normal 13px Verdana, sans-serif' } ");
        XYSeries seriesData = new XYSeries(property, colCfg);
        seriesData.setSeriesPoints(getPoints(seriesData, sortedData));

        chart.addSeries(seriesData);

       return chart;
    }
	
	private InvientCharts showAggregatedColumnWithRotatedLabels(InvientChartsConfig chartConfig, String property, Map<String, String> uri2Label, Map<String, Double> data){
	       
        chartConfig.getGeneralChartConfig().setType(SeriesType.COLUMN);
    
       
        
        Interval[] intervals = Intervals.aggregateDoubles(data, 5);
        List<String> categories = new ArrayList<String>();
        for(Interval interval : intervals){
        	categories.add("<="  + NumberFormat.getCurrencyInstance(Locale.UK).format(interval.getUpperBoundary()));
        }
        
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setCategories(categories);
        xAxis.setLabel(new XAxisDataLabel());
        xAxis.getLabel().setRotation(-45);
        xAxis.getLabel().setAlign(HorzAlign.RIGHT);
        xAxis.getLabel()
                .setStyle("{ font: 'normal 13px Verdana, sans-serif' }");
        LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
        xAxesSet.add(xAxis);
        chartConfig.setXAxes(xAxesSet);

        NumberYAxis yAxis = new NumberYAxis();
        yAxis.setMin(0.0);
        yAxis.setTitle(new AxisTitle("#houses"));
        LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
        yAxesSet.add(yAxis);
        chartConfig.setYAxes(yAxesSet);

        chartConfig.setLegend(new Legend(false));

//        chartConfig
//                .getTooltip()
//                .setFormatterJsFunc(
//                        "function() {"
//                                + " return '<b>'+ this.x +'</b><br/>'+ " + property + "' : '+ $wnd.Highcharts.numberFormat(this.y, 1) + "
//                                + " ' millions' " + "}");

        InvientCharts chart = new InvientCharts(chartConfig);

        ColumnConfig colCfg = new ColumnConfig();
        colCfg.setDataLabel(new DataLabel());
        colCfg.getDataLabel().setRotation(-90);
        colCfg.getDataLabel().setAlign(HorzAlign.RIGHT);
        colCfg.getDataLabel().setX(-3);
        colCfg.getDataLabel().setY(10);
        colCfg.getDataLabel().setColor(new RGB(255, 255, 255));
        colCfg.getDataLabel().setFormatterJsFunc(
                "function() {" + " return this.y; " + "}");
        colCfg.getDataLabel().setStyle(
                " { font: 'normal 13px Verdana, sans-serif' } ");
        XYSeries seriesData = new XYSeries(property, colCfg);
        seriesData.setSeriesPoints(getPoints(seriesData, intervals));

        chart.addSeries(seriesData);

       return chart;
    }
	
    
    private static LinkedHashSet<DecimalPoint> getPoints(Series series,
    		Map<String, Double> data) {
    	LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
		for(Entry<String, Double> entry : data.entrySet()){
				points.add(new DecimalPoint(series, entry.getValue()));
		}
		return points;
    }
    
    private static LinkedHashSet<DecimalPoint> getPoints(Series series,
    		Interval[] intervals) {
    	LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
		for(Interval interval : intervals){
			points.add(new DecimalPoint(series, interval.getSize()));
		}
		return points;
    }
    
    private static LinkedHashSet<DecimalPoint> getPoints(Series series,
    		List<Entry<String, Double>> data) {
    	LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
    	for(Entry<String, Double> entry : data){
			points.add(new DecimalPoint(series, entry.getValue()));
	}
		return points;
    }
}
