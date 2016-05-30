package gpig.group2.mapsservices.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Header pairs separated by semicolon, key/value are separated by equals.
 */
public class AddHeadersFilter implements Filter {

    private static final String INIT_PARAM_KEY = "Headers";
    private static final String EQUALS = "=";
    private static final String SEMICOLON = ";";

    private Map<String, String> headers = new HashMap<>();

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        for (String headerName : headers.keySet()) {
            httpResponse.setHeader(headerName, headers.get(headerName));
        }

        chain.doFilter(request, httpResponse);
    }

    private Map<String, String> getHeadersFromCsv(String strHeaders) {

        Map<String, String> headers = new HashMap<>();

        for (String strHeader : strHeaders.split(SEMICOLON)) {
            String[] headerComponents = strHeader.split(EQUALS);

            if (headerComponents.length != 2) {
                throw new IllegalArgumentException("Specified headers are malformed.");
            }

            headers.put(headerComponents[0].trim(), headerComponents[1].trim());
        }

        return headers;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String strHeaders = filterConfig.getInitParameter(INIT_PARAM_KEY);
        if (strHeaders == null) {
            return;
        }

        try {
            headers = getHeadersFromCsv(strHeaders);
        }
        catch (IllegalArgumentException e) {
            throw new ServletException(e);
        }
    }
}
