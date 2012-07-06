package org.autosparql.tbsl.widget;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.autosparql.tbsl.UserSession;

import com.hp.hpl.jena.rdf.model.Literal;
import com.ibm.icu.text.SimpleDateFormat;
import com.invient.vaadin.charts.Color.RGB;
import com.invient.vaadin.charts.Color.RGBA;
import com.invient.vaadin.charts.Gradient;
import com.invient.vaadin.charts.Gradient.LinearGradient.LinearColorStop;
import com.invient.vaadin.charts.InvientCharts;
import com.invient.vaadin.charts.InvientCharts.DateTimePoint;
import com.invient.vaadin.charts.InvientCharts.DateTimeSeries;
import com.invient.vaadin.charts.InvientCharts.DecimalPoint;
import com.invient.vaadin.charts.InvientCharts.Series;
import com.invient.vaadin.charts.InvientCharts.SeriesType;
import com.invient.vaadin.charts.InvientChartsConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AreaConfig;
import com.invient.vaadin.charts.InvientChartsConfig.AxisBase.AxisTitle;
import com.invient.vaadin.charts.InvientChartsConfig.DateTimeAxis;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.Spacing;
import com.invient.vaadin.charts.InvientChartsConfig.GeneralChartConfig.ZoomType;
import com.invient.vaadin.charts.InvientChartsConfig.MarkerState;
import com.invient.vaadin.charts.InvientChartsConfig.NumberYAxis;
import com.invient.vaadin.charts.InvientChartsConfig.SeriesState;
import com.invient.vaadin.charts.InvientChartsConfig.SymbolMarker;
import com.invient.vaadin.charts.InvientChartsConfig.XAxis;
import com.invient.vaadin.charts.InvientChartsConfig.YAxis;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TimeChartWindow extends Window{
	
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");
	
	public TimeChartWindow() {
		showTimeChart();
	}
	
	private void showTimeChart(){
		InvientChartsConfig chartConfig = new InvientChartsConfig();
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
        DateTimeSeries dateTimeSeries = new DateTimeSeries("USD to EUR",
                SeriesType.SCATTER, serieaAreaCfg);

        dateTimeSeries.addPoint((DateTimePoint[]) getTimePoints(dateTimeSeries).toArray(new DateTimePoint[0]));
        chart.addSeries(dateTimeSeries);
        
        chart.setSizeFull();

        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        setContent(content);
        
        content.addComponent(chart);
        content.setExpandRatio(chart, 1f);
	}
	
	private LinkedHashSet<DateTimePoint> getTimePoints(Series series){
		Map<String, Set<Object>> data = UserSession.getManager().getDataForProperty("http://dbpedia.org/ontology/releaseDate");
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
	
	private static long getPointStartDate(int year, int month, int day) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
	
	private LinkedHashSet<DateTimePoint> getDateTimePoints(Series series,
            double... values) {
        LinkedHashSet<DateTimePoint> points = new LinkedHashSet<DateTimePoint>();
        for (double value : values) {
            points.add(new DateTimePoint(series, value));
        }
        return points;
    }
	
	private static LinkedHashSet<DecimalPoint> getPoints(Series series,
            double[]... values) {
        LinkedHashSet<DecimalPoint> points = new LinkedHashSet<DecimalPoint>();
        for (double[] value : values) {
            Double x, y = null;
            if (value.length == 0)
                continue;
            if (value.length == 2) {
                x = value[0];
                y = value[1];
            } else {
                x = value[0];
            }
            points.add(new DecimalPoint(series, x, y));
        }
        return points;
    }
	
	
	public static void main(String[] args) throws ParseException {
	System.out.println(new SimpleDateFormat("yyyy-mm-dd").parse("2000-11-09"));
	AreaConfig serieaAreaCfg = new AreaConfig();
//  serieaAreaCfg.setPointStart((double) getPointStartDate(2006, 0, 01));
//  serieaAreaCfg.setPointInterval(24 * 3600 * 1000.0);
  // Series
  DateTimeSeries dateTimeSeries = new DateTimeSeries("USD to EUR",
          SeriesType.AREA, serieaAreaCfg);
  DateTimePoint point = new DateTimePoint(dateTimeSeries, new SimpleDateFormat("yyyy-mm-dd").parse("2000-11-09"), 1);
	System.out.println(point);
	System.out.println(point.getX());
	}
}
