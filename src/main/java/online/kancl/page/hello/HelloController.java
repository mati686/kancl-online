package online.kancl.page.hello;

import online.kancl.server.Controller;
import online.kancl.server.template.PebbleTemplateRenderer;
import spark.Request;
import spark.Response;

public class HelloController extends Controller {
    private final PebbleTemplateRenderer pebbleTemplateRenderer;

    public HelloController(PebbleTemplateRenderer pebbleTemplateRenderer){
        this.pebbleTemplateRenderer = pebbleTemplateRenderer;
    }
    @Override
    public String get(Request request, Response response) {
        return pebbleTemplateRenderer.renderDefaultControllerTemplate(this, null);
    }
}
