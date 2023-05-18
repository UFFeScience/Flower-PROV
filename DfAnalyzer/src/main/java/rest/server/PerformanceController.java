package rest.server;

import di.object.process.DaemonDI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author vitor
 */

@RestController
@RequestMapping("/performance")
public class PerformanceController {
    
    @Autowired
    private DaemonDI daemonDI;
    
    @RequestMapping(method = RequestMethod.GET, value = "/{header}")
    public String performance(@PathVariable("header") boolean header) {
        if (this.daemonDI.queue == null) {
            return "Performance data are not available right now, since DfAnalyzer is not online yet.";
        }        
        return this.daemonDI.getPerformanceData(header);
    }
    
}
