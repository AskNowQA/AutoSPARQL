package org.aksw.autosparql.tbsl.gui.vaadin;

import java.io.BufferedWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.ui.Window;

public class TBSLAppServlet extends ApplicationServlet{
	@Override
    protected void writeAjaxPageHtmlVaadinScripts(Window window,
            String themeName, Application application, BufferedWriter page,
            String appUrl, String themeUri, String appId,
            HttpServletRequest request) throws ServletException, IOException {
        page.write("<script type=\"text/javascript\">\n");
        page.write("//<![CDATA[\n");

        page.write("document.write(\"<script language='javascript' src='" + request.getContextPath() + "/VAADIN/jquery/jquery-1.4.4.min.js'><\\/script>\");\n");
        page.write("document.write(\"<script language='javascript' src='" + request.getContextPath() + "/VAADIN/js/highcharts.js'><\\/script>\");\n");
//        page.write("document.write(\"<script language='javascript' src='./js/modules/exporting.js'><\\/script>\");\n");
        page.write("//]]>\n</script>\n");
        super.writeAjaxPageHtmlVaadinScripts(window, themeName, application,
                page, appUrl, themeUri, appId, request);
    }
}
