package rs.pds.booking.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix="security.api-key")
public class ApiKeyProperties {
    private boolean enabled = true;
    private String headerName = "X-API-Key";
    private String value =  "sifra123";
    private List<String> whitelist = new ArrayList<>();

    //getter-i i setter-i
    public boolean isEnabled() {return enabled;}
    public void setEnabled(boolean enabled) {this.enabled = enabled;}
    public String getHeaderName() {return headerName;}
    public void setHeaderName(String headerName) {this.headerName = headerName;}
    public String getValue() {return value;}
    public void setValue(String value) {this.value = value;}
    public List<String> getWhitelist() {return whitelist;}
    public void setWhitelist(List<String> whitelist) {this.whitelist = whitelist;}
}
