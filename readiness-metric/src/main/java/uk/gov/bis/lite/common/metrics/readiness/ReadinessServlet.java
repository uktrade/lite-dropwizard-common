package uk.gov.bis.lite.common.metrics.readiness;

import com.google.inject.Inject;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *   An HttpServlet to expose the result of a {@link ReadinessService} to be used as a readiness metric.
 *   Will response with {@code 200 (OK)} if the service is ready and {@code 500 (BAD RESPONSE)} otherwise, see {@link ReadinessService#isReady()}.
 * </p>
 * <p>
 *   The response content type is {@code application/json} and body contains JSON defined in {@link ReadinessService#readinessJson()}.
 * </p>
 * <p>
 *   This class is expected to be used with the Google Guice library to inject the ReadinessService used by the servlet.
 * </p>
 */
public class ReadinessServlet extends HttpServlet {

  private final ReadinessService readinessService;

  @Inject
  public ReadinessServlet(ReadinessService readinessService) {
    this.readinessService = readinessService;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");
    if (readinessService.isReady()) {
      resp.setStatus(HttpServletResponse.SC_OK);
    } else {
      resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    String readinessJsonString = readinessService.readinessJson().toString();

    final PrintWriter writer = resp.getWriter();
    try {
      writer.print(readinessJsonString);
    } finally {
      writer.close();
    }
  }
}