package org.autosparql.tbsl.widget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.autosparql.tbsl.model.Interval;
import org.autosparql.tbsl.util.Intervals;
import org.autosparql.tbsl.util.Labels;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.Color.RGBA;
import com.invient.vaadin.charts.Gradient;
import com.invient.vaadin.charts.Gradient.LinearGradient.LinearColorStop;
import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.PointSelectEvent;
import com.invient.vaadin.charts.InvientCharts.Series;
import com.invient.vaadin.charts.InvientCharts.SeriesClickEvent;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientCharts.XYSeries;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AreaConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitleAlign;
import com.invient.vaadin.charts.InvientChartsConfig.BarConfig;
import com.invient.vaadin.charts.InvientChartsConfig.CategoryAxis;
import com.invient.vaadin.charts.InvientChartsConfig.ColumnConfig;
import com.invient.vaadin.charts.InvientChartsConfig.DataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.DateTimeAxis;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.Spacing;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.ZoomType;
import com.invient.vaadin.charts.InvientChartsConfig.HorzAlign;
import com.invient.vaadin.charts.InvientChartsConfig.Legend;
import com.invient.vaadin.charts.InvientChartsConfig.Legend.Layout;
import com.invient.vaadin.charts.InvientChartsConfig.MarkerState;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.Position;
import com.invient.vaadin.charts.InvientChartsConfig.SeriesState;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker;
import com.invient.vaadin.charts.InvientChartsConfig.VertAlign;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.XAxisDataLabel;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class Charts {
	
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");
	
	private static List<String> barChartProperties = Arrays.asList(new String[]{});
	private static List<String> columnChartProperties = Arrays.asList(new String[]{"http://diadem.cs.ox.ac.uk/ontologies/real-estate#hasPrice"});
	private static List<String> timeSeriesChartProperties = Arrays.asList(new String[]{});
	
	public static Window getChart(String question, String propertyURI, XSDDatatype datatype, Map<String, String> uri2Label, Map<String, Set<Object>> data){
		Window wnd = new Window();
		wnd.setWidth("900px");
		wnd.setHeight("900px");
		
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeUndefined();
//		wnd.setContent(mainLayout);
		
		Panel p = new Panel();
		p.setSizeFull();
		p.setContent(mainLayout);
		wnd.setContent(p);
		
		InvientChartsConfig chartConfig = new InvientChartsConfig();
		String propertyLabel = Labels.getLabel(propertyURI);
		String title = propertyLabel + " for " + "\"" + question + "\"";
		chartConfig.getTitle().setText(title);
		
		InvientCharts chart = createChart(chartConfig, propertyURI, propertyLabel, datatype, uri2Label, data);
		if(chart != null){
			chart.setSizeFull();
			chart.setHeight("800px");
			mainLayout.addComponent(chart);
			return wnd;
		}
		return null;
	}
	
	public static InvientCharts createChart(InvientChartsConfig chartConfig, String propertyURI, String propertyLabel, XSDDatatype datatype, Map<String, String> uri2Label, Map<String, Set<Object>> data){
		InvientCharts chart = getChartByPropertyURI(chartConfig, propertyURI, propertyLabel, datatype, uri2Label, data);
		if(chart == null){
			chart = getChartByDatatype(chartConfig, propertyURI, propertyLabel, datatype, uri2Label, data);
		}
		return chart;
	}
	
	public static InvientCharts getChartByPropertyURI(InvientChartsConfig chartConfig, String propertyURI, String propertyLabel, XSDDatatype datatype, Map<String, String> uri2Label, Map<String, Set<Object>> data){
		if(barChartProperties.contains(propertyURI)){
			return createBarChart(chartConfig, propertyLabel, uri2Label, data);
		} else if(columnChartProperties.contains(propertyURI)){
//			return showColumnWithRotatedLabels(chartConfig, propertyLabel, uri2Label, data);
			return showAggregatedColumnWithRotatedLabels(chartConfig, propertyLabel, uri2Label, data);
		} else if(timeSeriesChartProperties.contains(propertyURI)){
			return createTimeChart(chartConfig, propertyLabel, uri2Label, data);
		}
		return null;
	}
	
	public static InvientCharts getChartByDatatype(InvientChartsConfig chartConfig, String propertyURI, String propertyLabel, XSDDatatype datatype, Map<String, String> uri2Label, Map<String, Set<Object>> data){
		if(datatype != null){
			if(datatype == XSDDatatype.XSDdouble || datatype == XSDDatatype.XSDint 
					|| datatype == XSDDatatype.XSDinteger || datatype == XSDDatatype.XSDpositiveInteger ){
				return createBarChart(chartConfig, propertyLabel, uri2Label, data);
			} else if(datatype == XSDDatatype.XSDdate || datatype == XSDDatatype.XSDdateTime){
				return createTimeChart(chartConfig, propertyLabel, uri2Label, data);
			}
			throw new RuntimeException("Datatype " + datatype + " currently not supported.");
		}
		return null;
	}
		
	
	private static InvientCharts createTimeChart(InvientChartsConfig chartConfig, String property, Map<String, String> uri2Label, Map<String, Set<Object>> data){
        chartConfig.getGeneralChartConfig().setZoomType(ZoomType.X);
        chartConfig.getGeneralChartConfig().setSpacing(new Spacing());
        chartConfig.getGeneralChartConfig().getSpacing().setRight(20);

        chartConfig.getSubtitle().setText(
                "Click and drag in the plot area to zoom in");

        DateTimeAxis xAxis = new DateTimeAxis();
        xAxis.setMaxZoom(14 * 24 * 3600000);
        LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
        xAxesSet.add(xAxis);
        chartConfig.setXAxes(xAxesSet);

        NumberYAxis yAxis = new NumberYAxis();
        yAxis.setTitle(new AxisTitle(""));
        yAxis.setMin(1d);
        yAxis.setMax(1d);
        yAxis.setStartOnTick(true);
        yAxis.setShowFirstLabel(false);
        LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
        yAxesSet.add(yAxis);
        chartConfig.setYAxes(yAxesSet);

        chartConfig.getTooltip().setShared(true);

        chartConfig.getLegend().setEnabled(false);

        // Set plot options
        AreaConfig areaCfg = new AreaConfig();

        List<LinearColorStop> colorStops = new ArrayList<Gradient.LinearGradient.LinearColorStop>();
        colorStops.add(new LinearColorStop(0, new RGB(69, 114, 167)));
        colorStops.add(new LinearColorStop(1, new RGBA(2, 0, 0, 0)));
        // Fill color
//        areaCfg.setFillColor(new Gradient.LinearGradient(0, 0, 0, 300, colorStops));

        areaCfg.setLineWidth(1);
        areaCfg.setShadow(false);
        areaCfg.setHoverState(new SeriesState());
        areaCfg.getHoverState().setLineWidth(1);
        SymbolMarker marker;
        areaCfg.setMarker(marker = new SymbolMarker(false));
        marker.setHoverState(new MarkerState());
        marker.getHoverState().setEnabled(true);
        marker.getHoverState().setRadius(5);

        chartConfig.addSeriesConfig(areaCfg);

        InvientCharts chart = new InvientCharts(chartConfig);

        // Area configuration
        AreaConfig serieaAreaCfg = new AreaConfig();
//        serieaAreaCfg.setPointStart((double) getPointStartDate(2006, 0, 01));
//        serieaAreaCfg.setPointInterval(24 * 3600 * 1000.0);
        // Series
        DateTimeSeries dateTimeSeries = new DateTimeSeries(property,
                SeriesType.SCATTER, serieaAreaCfg);

        dateTimeSeries.addPoint((DateTimePoint[]) getTimePoints(dateTimeSeries, data).toArray(new DateTimePoint[0]));
        chart.addSeries(dateTimeSeries);
        
        return chart;
	}
	
	public static InvientCharts createColumnChart(InvientChartsConfig chartConfig, String property, Map<String, String> uri2Label, Map<String, Set<Object>> data){
		chartConfig.getGeneralChartConfig().setType(SeriesType.COLUMN);

        SortedMap<String, Set<Object>> sortedData = new TreeMap<String, Set<Object>>(data);
        List<String> categories = new ArrayList<String>();
        String label;
        for(String uri : sortedData.keySet()){
        	label = uri2Label.get(uri);
        	if(label == null){
        		label = Labels.getLabelForResource(uri);
        	}
        	categories.add(label);
        }
        
        CategoryAxis xAxisMain = new CategoryAxis();
        xAxisMain.setCategories(categories);
        xAxisMain.setLabel(new XAxisDataLabel());
        xAxisMain.getLabel().setRotation(-90);
        LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
        xAxesSet.add(xAxisMain);
        chartConfig.setXAxes(xAxesSet);

        NumberYAxis yAxis = new NumberYAxis();
        yAxis.setMin(0.0);
        yAxis.setTitle(new AxisTitle(property));
        yAxis.getTitle().setAlign(AxisTitleAlign.HIGH);
        LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
        yAxesSet.add(yAxis);
        chartConfig.setYAxes(yAxesSet);

//        chartConfig
//                .getTooltip()
//                .setFormatterJsFunc(
//                        "function() {"
//                                + " return '' + this.series.name +': '+ this.y +' millions';"
//                                + "}");

        BarConfig barCfg = new BarConfig();
        barCfg.setDataLabel(new DataLabel());
        chartConfig.addSeriesConfig(barCfg);

        Legend legend = new Legend();
        legend.setLayout(Layout.VERTICAL);
        legend.setPosition(new Position());
        legend.getPosition().setAlign(HorzAlign.RIGHT);
        legend.getPosition().setVertAlign(VertAlign.TOP);
        legend.getPosition().setX(-100);
        legend.getPosition().setY(100);
        legend.setFloating(true);
        legend.setBorderWidth(1);
        legend.setBackgroundColor(new RGB(255, 255, 255));
        legend.setShadow(true);
//        chartConfig.setLegend(legend);

        chartConfig.getCredit().setEnabled(false);

        InvientCharts chart = new InvientCharts(chartConfig);

        XYSeries seriesData = new XYSeries(property);
        seriesData.setSeriesPoints(getPoints(seriesData, sortedData));
        chart.addSeries(seriesData);

        
        return chart;
	}
	
	
	private static InvientCharts createBarChart(InvientChartsConfig chartConfig, String property, Map<String, String> uri2Label, Map<String, Set<Object>> data){
        chartConfig.getGeneralChartConfig().setType(SeriesType.BAR);

        SortedMap<String, Set<Object>> sortedData = new TreeMap<String, Set<Object>>(data);
        List<String> categories = new ArrayList<String>();
        String label;
        for(String uri : sortedData.keySet()){
        	label = uri2Label.get(uri);
        	if(label == null){
        		label = Labels.getLabelForResource(uri);
        	}
        	categories.add(label);
        }
        
        CategoryAxis xAxisMain = new CategoryAxis();
        xAxisMain.setCategories(categories);
        LinkedHashSet<XAxis> xAxesSet = new LinkedHashSet<InvientChartsConfig.XAxis>();
        xAxesSet.add(xAxisMain);
        chartConfig.setXAxes(xAxesSet);

        NumberYAxis yAxis = new NumberYAxis();
        yAxis.setMin(0.0);
        yAxis.setTitle(new AxisTitle(property));
        yAxis.getTitle().setAlign(AxisTitleAlign.HIGH);
        LinkedHashSet<YAxis> yAxesSet = new LinkedHashSet<InvientChartsConfig.YAxis>();
        yAxesSet.add(yAxis);
        chartConfig.setYAxes(yAxesSet);

//        chartConfig
//                .getTooltip()
//                .setFormatterJsFunc(
//                        "function() {"
//                                + " return '' + this.series.name +': '+ this.y +' millions';"
//                                + "}");

        BarConfig barCfg = new BarConfig();
        barCfg.setDataLabel(new DataLabel());
        chartConfig.addSeriesConfig(barCfg);

        Legend legend = new Legend();
        legend.setLayout(Layout.VERTICAL);
        legend.setPosition(new Position());
        legend.getPosition().setAlign(HorzAlign.RIGHT);
        legend.getPosition().setVertAlign(VertAlign.TOP);
        legend.getPosition().setX(-100);
        legend.getPosition().setY(100);
        legend.setFloating(true);
        legend.setBorderWidth(1);
        legend.setBackgroundColor(new RGB(255, 255, 255));
        legend.setShadow(true);
//        chartConfig.setLegend(legend);

        chartConfig.getCredit().setEnabled(false);

        InvientCharts chart = new InvientCharts(chartConfig);

        XYSeries seriesData = new XYSeries(property);
        seriesData.setSeriesPoints(getPoints(seriesData, sortedData));
        chart.addSeries(seriesData);

        
        return chart;
	}
	
	public static InvientCharts showColumnWithRotatedLabels(InvientChartsConfig chartConfig, String property, Map<String, String> uri2Label, Map<String, Set<Object>> data){
       
        chartConfig.getGeneralChartConfig().setType(SeriesType.COLUMN);
    
        SortedMap<String, Set<Object>> sortedData = new TreeMap<String, Set<Object>>(data);
        List<String> categories = new ArrayList<String>();
        String label;
        for(String uri : sortedData.keySet()){
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
        yAxis.setTitle(new AxisTitle(property));
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
        seriesData.setSeriesPoints(getPoints(seriesData, data));

        chart.addSeries(seriesData);

       return chart;
    }
	
	public static InvientCharts showAggregatedColumnWithRotatedLabels(InvientChartsConfig chartConfig, String property, Map<String, String> uri2Label, Map<String, Set<Object>> data){
	       
        chartConfig.getGeneralChartConfig().setType(SeriesType.COLUMN);
    
        Map<String, Object> sample = Intervals.sample(data);
        Map<String, Double> castData = new HashMap<String, Double>();
        for(Entry<String, Object> entry : sample.entrySet()){
        	castData.put(entry.getKey(), (Double) entry.getValue());
        }
        
        Interval[] intervals = Intervals.aggregateDoubles(castData, 5);
        List<String> categories = new ArrayList<String>();
        for(Interval interval : intervals){
        	categories.add("<="  + interval.getUpperBoundary());
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
        yAxis.setTitle(new AxisTitle(property));
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
        
        chart.addListener(new InvientCharts.PointSelectListener() {

            @Override
            public void pointSelected(PointSelectEvent pointSelectEvent) {
            	System.out.println(pointSelectEvent);
                if (pointSelectEvent.getPoint() instanceof DecimalPoint) {
                	System.out.println(pointSelectEvent.getCategory());
                	System.out.println(pointSelectEvent.getPoint());
                } else {
                }
            }
        });
        chart.addListener(new InvientCharts.SeriesClickListerner() {

            @Override
            public void seriesClick(SeriesClickEvent seriesClickEvent) {
                System.out.println(seriesClickEvent.getNearestPoint());

            }
        });

       return chart;
    }
	
    
    private static LinkedHashSet<DecimalPoint> getPoints(Series series,
    		Map<String, Set<Object>> data) {
    	LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
		for(Entry<String, Set<Object>> entry : data.entrySet()){
			Object value = entry.getValue().iterator().next();
			Double doubleValue = null;
			if(value instanceof Double){
				doubleValue = (Double) value;
			} else if(value instanceof Integer){
				doubleValue = ((Integer)value).doubleValue();
			} else if(value instanceof String){
				try {
					doubleValue = Double.parseDouble((String) value);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			if(doubleValue != null){
				points.add(new DecimalPoint(series, doubleValue));
			}
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
    
    private static LinkedHashSet<DateTimePoint> getTimePoints(Series series, Map<String, Set<Object>> data){
		LinkedHashSet<DateTimePoint> points = new LinkedHashSet<DateTimePoint>();
		for(Entry<String, Set<Object>> entry : data.entrySet()){
			String value = (String) entry.getValue().iterator().next();
			try {
				Date date = df.parse(value);
				DateTimePoint p = new DateTimePoint(series, date, 1.0);
				p.setName(entry.getKey());
				points.add(p);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return points;
	}
	
	

}
