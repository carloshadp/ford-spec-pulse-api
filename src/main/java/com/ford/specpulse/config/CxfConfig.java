package com.ford.specpulse.config;

import com.ford.specpulse.soap.JwtSoapInterceptor;
import com.ford.specpulse.soap.SpecPulseSoapService;
import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao Apache CXF — expoe o WebService SOAP.
 *
 * WSDL: http://localhost:8080/soap/spec-pulse?wsdl
 *
 * Seguranca:
 * - JwtSoapInterceptor valida Bearer token no SOAP Header
 *   para operacoes protegidas (consultarFichaTecnica, criarComparacao).
 * - Habilitado via specpulse.soap.auth-habilitada=true
 */
@Configuration
public class CxfConfig {

    private final Bus bus;
    private final SpecPulseSoapService soapService;
    private final JwtSoapInterceptor jwtInterceptor;

    public CxfConfig(Bus bus, SpecPulseSoapService soapService,
                     JwtSoapInterceptor jwtInterceptor) {
        this.bus = bus;
        this.soapService = soapService;
        this.jwtInterceptor = jwtInterceptor;
    }

    @Bean
    public Endpoint specPulseEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, soapService);
        endpoint.getInInterceptors().add(jwtInterceptor);
        endpoint.publish("/spec-pulse");
        return endpoint;
    }
}