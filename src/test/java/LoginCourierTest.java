import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.hasKey;

public class LoginCourierTest {


    private Courier courier;
    private Courier courierWithoutPassword;
    private CourierClient courierClient;
    private int courierId;


    @Before
    public void setUp(){
        courier = CourierGenerator.getDefault();
        courierWithoutPassword = CourierGenerator.getCourierWithLoginOnly();
        courierClient = new CourierClient();
    }

    @After
    public void tearDown(){
        courierClient.deleteCourier(courierId);
    }

    @Test
    @DisplayName("Авторизация курьера (позитивный)")
    public void courierCanBeLoginTest(){
        ValidatableResponse createdResponse = courierClient.createCourier(courier);
        ValidatableResponse loginResponse = courierClient.login(CourierCredentials.from(courier));

        int loginStatusCode = loginResponse.extract().statusCode();
        Assert.assertEquals("Не верный статус код авторизации", SC_OK, loginStatusCode);

        loginResponse.body("$", hasKey("id"));
        courierId = loginResponse.extract().path("id");
        Assert.assertTrue("Пустой ID" , courierId > 0);
    }

    @Test
    @DisplayName("Авторизация курьера (негативный)")
    public void courierCanNotBeLoginWithWrongPasswordTest(){
        ValidatableResponse createdResponse = courierClient.createCourier(courier);
        ValidatableResponse loginResponse = courierClient.login(CourierCredentials.CourierCredentialsWithWrongPassword(courier));

        int loginStatusCode = loginResponse.extract().statusCode();
        Assert.assertEquals("Не верный статус код авторизации", SC_NOT_FOUND, loginStatusCode);
        String message = loginResponse.extract().path("message");
        Assert.assertEquals("Ответ не получен", "Учетная запись не найдена", message);
    }

    @Test
    @DisplayName("courier login only login")
    public void courierCanNotBeLoginWithoutPassword(){
        ValidatableResponse createdResponse = courierClient.createCourier(courier);
        ValidatableResponse loginResponse = courierClient.login(CourierCredentials.from(courierWithoutPassword));

        int loginStatusCode = loginResponse.extract().statusCode();
        Assert.assertEquals("Не верный статус код авторизации", SC_BAD_REQUEST, loginStatusCode);
        String message = loginResponse.extract().path("message");
        Assert.assertEquals("Ответ не получен", "Недостаточно данных для входа", message);
    }

}
