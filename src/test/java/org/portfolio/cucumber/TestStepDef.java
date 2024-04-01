package org.portfolio.cucumber;


import com.google.common.collect.Multimap;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.rtportfolio.PortfolioUpdateWorker;
import org.rtportfolio.model.Position;

import java.util.List;
import java.util.Map;

public class TestStepDef {
    private PortfolioUpdateWorker portfolioUpdateWorker;
    private Map<String, Position> symbol2PositionMap;
    private Multimap<String, String> symbol2OptionMap;

    @Given("the following positions in the portfolio")
    public void the_following_positions_in_the_portfolio(DataTable dataTable) {
        final List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> row : rows) {

        }
    }

    @Given("the following instrument available in the database")
    public void the_following_instrument_available_in_the_database(DataTable dataTable) {

    }

    @Then("start the PortfolioUpdateWorker")
    public void start_the_portfolio_update_worker() {

    }

    @When("suppose we receive the below price update")
    public void suppose_we_receive_the_below_price_update(io.cucumber.datatable.DataTable dataTable) {

    }

    @Then("portfolio subscriber should receive the below update")
    public void portfolio_subscriber_should_receive_the_below_update(io.cucumber.datatable.DataTable dataTable) {

    }
}