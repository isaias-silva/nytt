package dev.nytt.config;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

@OpenAPIDefinition(
        info = @Info(
                title = "Nytt",
                version = "1.0",
                description = "api para processamento e disponibilização de arquivos.",
                contact = @Contact(
                        name = "dev",
                        email = "isaiasgarraeluta@gmail.com",
                        url = "https://zack.io"
                ),
                license = @License(
                        name = "Licença Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        )
)
public class SwaggerConfig extends Application {
}
