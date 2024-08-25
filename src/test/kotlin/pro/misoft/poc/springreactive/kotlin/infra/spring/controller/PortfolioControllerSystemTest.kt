package pro.misoft.poc.springreactive.kotlin.infra.spring.controller

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock
import com.maciejwalkowiak.wiremock.spring.EnableWireMock
import com.maciejwalkowiak.wiremock.spring.InjectWireMock
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import pro.misoft.poc.springreactive.kotlin.business.model.account.AccountMother
import java.math.BigDecimal


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(
    ConfigureWireMock(name = "account-service", property = "finstar.baseurl"),
    ConfigureWireMock(name = "forex-service", property = "forex.baseurl")
)
class PortfolioControllerSystemTest : AbstractSystemTest() {
    val userId = "testUserIdToBeResolvedFromJWT"

    @InjectWireMock("account-service")
    private lateinit var accountWiremock: WireMockServer

    @InjectWireMock("forex-service")
    private lateinit var forexWiremock: WireMockServer

    @Test
    fun `should retrieve portfolio overview with total balance from Rest API`() {
        val currency = "BTC"
        val refCurrency = "EUR"
        val balance = BigDecimal(10)
        val account = AccountMother.newAccount(currency, balance)
        val rate = BigDecimal(60000)


        val getAccountsUrl = urlEqualTo("/users/$userId/accounts")
        accountWiremock.stubFor(
            get(getAccountsUrl)
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                        [
                            { "id": "${account.id}", "currency": "${account.currency}", "domain": "Crypto" }
                        ]
                        """
                        )
                )
        );

        val getBalanceUrl = urlEqualTo("/accounts/${account.id}/balance")
        accountWiremock.stubFor(
            get(getBalanceUrl)
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(account.balance.toString())
                )
        );

        val getRateUrl = urlEqualTo("/rates/${currency}-${refCurrency}")
        forexWiremock.stubFor(
            get(getRateUrl)
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                                    { "mid": "$rate" }
                                  """
                        )
                )
        )

        Given {
            contentType(ContentType.JSON)
            headers("traceparent", "00-490aeb1c01cdfbe34b2898aa373c1e55-b8899e74d55dc066-00")
        } When {
            get("/v1/portfolio?currency=$refCurrency")
        } Then {
            statusCode(200)
            body("totalMarketValue.formatted", Matchers.equalTo("€ 600,000.00"))
        }

        accountWiremock.verify(getRequestedFor(getAccountsUrl))
        accountWiremock.verify(getRequestedFor(getBalanceUrl))
        forexWiremock.verify(getRequestedFor(getRateUrl))
    }
}