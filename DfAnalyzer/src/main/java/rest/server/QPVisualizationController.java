package rest.server;

import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author Débora
 */
@RestController

public class QPVisualizationController {
    
@PostMapping("/query_processing")
    ModelAndView visualization(@RequestBody String csv) throws IOException {
        CSVTable tbl = new CSVTable(csv);       
        ModelAndView modelAndView = new ModelAndView("query_processing/visualization");
        modelAndView.addObject("table", tbl);
        return modelAndView;
    }
}