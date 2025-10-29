package rs.pds.booking.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ApiKeyFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);
    private final ApiKeyProperties properties;
    private final AntPathMatcher matcher = new AntPathMatcher();

    public ApiKeyFilter(ApiKeyProperties properties) {
        this.properties = properties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if(!properties.isEnabled()){
            return chain.filter(exchange);
        }

        var path = exchange.getRequest().getPath().value();

        for(String pattern : properties.getWhitelist()){
            if(matcher.match(pattern,path)){
                return chain.filter(exchange);
            }
        }

        var headerName = properties.getHeaderName();
        var provided = exchange.getRequest().getHeaders().getFirst(headerName);

        if(provided != null && provided.equals(properties.getValue())){
            return chain.filter(exchange);
        }

        log.warn("API key promasen za path: {}; headerName: {}; provided: {}", path, headerName, provided);

        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var body = ("{\"error\":\"unauthorized\",\"message\":\"Missing or invalid API key\"}").getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
