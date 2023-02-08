package nl.codefoundry.tellodroneserver;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "TelloDroneAPI",
                version = "1.0"
        )
)
public class TelloDroneServerApplication {
    public static void main(String[] arguments) {
        Micronaut.run(TelloDroneServerApplication.class, arguments);
    }
}