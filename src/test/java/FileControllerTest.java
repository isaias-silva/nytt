import dev.nytt.entities.FileEntity;
import dev.nytt.services.FileService;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;
import static io.restassured.RestAssured.given;


@QuarkusTest
public class FileControllerTest {
    @Inject
    static FileService fileServiceMock;

    @BeforeAll
    public static void setup() {
        fileServiceMock = Mockito.mock(FileService.class);

        QuarkusMock.installMockForType(fileServiceMock, FileService.class);
    }

    @Test
    public void returnFile() {


        FileEntity fileEntityMock = new FileEntity("aaa-bbb", "aaa.jpg");
        when(fileServiceMock.getFileEntity("aaaa")).thenReturn(fileEntityMock);
        given().get("/file?externalId=aaa-bbb").then().statusCode(200);
    }

    @Test
    public void fileNotFound() {
        given()
                .when().get("/file")
                .then()
                .statusCode(404);
    }
}
