package com.sippy.wrapper.parent;

import com.sippy.wrapper.parent.database.DatabaseConnection;
import com.sippy.wrapper.parent.database.dao.TnbDao;
import com.sippy.wrapper.parent.request.GetTnbListRequest;
import com.sippy.wrapper.parent.request.JavaTestRequest;
import com.sippy.wrapper.parent.response.JavaTestResponse;
import com.sippy.wrapper.parent.response.Tnb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.*;
import java.util.stream.Stream;

@Stateless
public class WrappedMethods {

  private static final Logger LOGGER = LoggerFactory.getLogger(WrappedMethods.class);

  @EJB DatabaseConnection databaseConnection;

  @RpcMethod(name = "javaTest", description = "Check if everything works :)")
  public Map<String, Object> javaTest(JavaTestRequest request) {
    JavaTestResponse response = new JavaTestResponse();

    int count = databaseConnection.getAllTnbs().size();

    LOGGER.info("the count is: " + count);

    response.setId(request.getId());
    String tempFeeling = request.isTemperatureOver20Degree() ? "warm" : "cold";
    response.setOutput(
        String.format(
            "%s has a rather %s day. And he has %d tnbs", request.getName(), tempFeeling, count));

    Map<String, Object> jsonResponse = new HashMap<>();
    jsonResponse.put("faultCode", "200");
    jsonResponse.put("faultString", "Method success");
    jsonResponse.put("something", response);

    return jsonResponse;
  }

  @RpcMethod(name = "getTnbList", description = "Get the TnbList")
  public Map<String, Object> getTnbList(GetTnbListRequest request){
    LOGGER.info("Received getTnbList request for tnb: {}", request.number());
    final List<TnbDao> allTnbs = databaseConnection.getAllTnbs();
    final List<TnbDao> filteredTnbs = allTnbs.stream().filter(tnbDao -> !List.of("D146", "D218", "D248").contains(tnbDao.getTnb())).toList();

    final Optional<TnbDao> maybeRequestedTnb = allTnbs.stream().filter(tnbDao -> request.number() != null && request.number().equals(tnbDao.getTnb())).findFirst();

    final TnbDao alwaysAddedTnb = new TnbDao();
    alwaysAddedTnb.setTnb("D001");
    alwaysAddedTnb.setName("Deutsche Telekom");
    final List<TnbDao> completeTnbs = Stream.concat(filteredTnbs.stream(), Stream.of(alwaysAddedTnb)).toList();
    final List<TnbDao> sortedTnbs = completeTnbs.stream().sorted(Comparator.comparing(a -> a.getName().toLowerCase())).toList();

    final List<Tnb> tnbs = sortedTnbs.stream().map(filteredTnb -> new Tnb(maybeRequestedTnb.isPresent() && maybeRequestedTnb.get().getTnb().equals(filteredTnb.getTnb()), filteredTnb.getName(), filteredTnb.getTnb())).toList();
    final Map<String, Object> jsonResponse = new HashMap<>();
    jsonResponse.put("faultCode", "200");
    jsonResponse.put("faultString", "Method success");
    jsonResponse.put("tnbs", tnbs);
    return jsonResponse;
  }
}
