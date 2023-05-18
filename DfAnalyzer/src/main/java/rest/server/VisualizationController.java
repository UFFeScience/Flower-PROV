package rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import rest.config.Dao;

/**
 * @author Thaylon Guedes, Vítor Silva
 * @email thaylongs@gmail.com, vitor.silva.sousa@gmail.com
 */
@Controller
@RequestMapping("/dfview")
public class VisualizationController {

    @Autowired
    private Dao dao;

    @GetMapping
    public String index() {
        return "dfview/ListDataflows";
    }
    
    @GetMapping("/help")
    public String dfaHelp() {
        return "dfview/dfa_help";
    }

//    @GetMapping("/query")
//    ModelAndView query(@RequestParam(required = false) Long dfId) {
//        if (dfId == null) {
//            return new ModelAndView("redirect:/dfview");
//        }
//        ModelAndView modelAndView = new ModelAndView("dfview/query_processor");
//        modelAndView.addObject("dataflow", dao.findDataflowById(dfId));
//        return modelAndView;
//    }
//
//    @ResponseBody
//    @PostMapping("/query")
//    ObjectNode processQuery(@RequestBody String body) {
//        System.out.println(body);
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectNode result = mapper.createObjectNode();
//        result.putArray("header").add("a").add("b");
//        ArrayNode row = result.putArray("rows");
//        row.addArray().add("1").add("2");
//        row.addArray().add("1").add("2");
//        row.addArray().add("1").add("2");
//        return result;
//    }

    @GetMapping("/visualization")
    ModelAndView visualization(@RequestParam Long dfId) {
        ModelAndView modelAndView = new ModelAndView("dfview/visualization");
        modelAndView.addObject("dataflow", dao.findDataflowById(dfId));
        return modelAndView;
    }
}
