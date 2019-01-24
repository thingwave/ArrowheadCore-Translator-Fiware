package eu.arrowhead.common.messages;

import java.util.HashMap;
import java.util.Map;
import org.hibernate.validator.constraints.NotEmpty;

public class OrchestrationStorePriorities {

  /*
    Key: Orchestration Store entry ID
    Value: the new priority the entry should have
   */
  @NotEmpty
  private Map<Long, Integer> priorities = new HashMap<>();

  public OrchestrationStorePriorities() {
  }

  public OrchestrationStorePriorities(Map<Long, Integer> priorities) {
    this.priorities = priorities;
  }

  public Map<Long, Integer> getPriorities() {
    return priorities;
  }

  public void setPriorities(Map<Long, Integer> priorities) {
    this.priorities = priorities;
  }
}
