package com.spacepocalypse.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.spacepocalypse.data.BeerDbAccess;
import com.spacepocalypse.engine.BeerSearchEngine;
import com.spacepocalypse.engine.MarkupEngine;
import com.spacepocalypse.pojo.MappedBeer;

public class BeerSearchServlet extends HttpServlet {
	private static final String VIEW_QUERIES_PARAM_NAME = "view_queries";
	private static final String BEER_NAME_PARAM_NAME = "beer_name";
	private static final String[] KEY_ORDER = {
		"abv",
		"descript"
	};
	private static final long serialVersionUID = 4630070655918253818L;
	
	private Map<String, List<String>> accessList;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html");  // Can also use "text/plain" or others.
		PrintWriter out = resp.getWriter();

		// Create output (the response):
		out.println("<html><head><title>BeerSearch</title></head>");
		out.println("<body><h1>BeerSearch</h1>");
		out.println("<form name=\"beer_search_form\" action=\"beersearch\" method=\"get\">");
		out.println("Search by name: " + "<input type=\"text\" name=\"" + BEER_NAME_PARAM_NAME + "\" /><input type=\"submit\" value=\"Submit\" />");
		out.println("</form><br /> <br />");
		
		if (req.getParameterMap().containsKey(BEER_NAME_PARAM_NAME) && req.getParameterMap().get(BEER_NAME_PARAM_NAME).length > 0) {
			String beerName = req.getParameterMap().get(BEER_NAME_PARAM_NAME)[0];
			
			// add to host->query[] table
			logRequest(req.getRemoteHost(), beerName);

			// do query and print results
			BeerSearchEngine.getInstance().doSearchByBeerName(out, beerName);
		} 
		
		if (req.getParameterMap().containsKey(VIEW_QUERIES_PARAM_NAME)) {
			out.println("<h1>Queries Run</h1>");
			
			for (String host : getAccessList().keySet()) {
				out.println("<h3>" + host + "</h3>");
				boolean notFirst = false;
				for (String eaQuery : getAccessList().get(host)) {
					if (notFirst) {
						out.print("], [");
					} else {
						out.print("[");
					}
					out.print(eaQuery);
					notFirst = true;
				}
				out.print("]");
				out.println("<br />");
			}
		}
		
		out.print("<form name=\"");
		out.print(VIEW_QUERIES_PARAM_NAME);
		out.println("\" action=\"beersearch\" method=\"get\">");
		
		out.print("<input type=\"hidden\" name=\"");
		out.print(VIEW_QUERIES_PARAM_NAME);
		out.println("\" value=\"1\" /><input type=\"submit\" value=\"View Queries\" /></form>");
		out.print(MarkupEngine.getInstance().getNavFooter());
		out.close();
	}
	
	public void logRequest(String remoteHost, String query) {
		if (getAccessList().containsKey(remoteHost)) { 
			getAccessList().get(remoteHost).add(query);
		} else {
			List<String> queries = new ArrayList<String>();
			queries.add(query);
			getAccessList().put(remoteHost, queries);
		}
	}

	public void setAccessList(Map<String, List<String>> accessList) {
		this.accessList = accessList;
	}
	public Map<String, List<String>> getAccessList() {
		if (accessList == null) {
			accessList = new ConcurrentHashMap<String, List<String>>();
		}
		return accessList;
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}


}
