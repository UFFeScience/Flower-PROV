package rest.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Débora
 */
@RestController

public class DownloadController {
    
@GetMapping("/downloadCSV")
    public void downloadCSV(HttpServletResponse response) throws IOException{
        String fileName = "/query_result.csv";
       
        response.setHeader("Content-Type", "aplication/xls");
        response.setHeader("Content-Disposition","attachment;filename=\"query_result.csv\"");
        
        URL url = null;
        //String completePath = url.getPath() + fileName;
        String currentPath = System.getProperty("user.dir").replace('\\', '/');
        String completePath = currentPath + fileName;
        OutputStream out = response.getOutputStream();
        try (InputStream in = new FileInputStream(completePath)) {
            while (in.available() > 0) {
                out.write(in.read());
            }
        } catch (Exception e) {
        }                    
    }
}