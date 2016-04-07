package eu.itesla_project.online;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public interface OnlineWorkflow {

	String getId();

	void start(OnlineWorkflowContext oCtx) throws Exception;

	void addOnlineApplicationListener(OnlineApplicationListener listener);

	void removeOnlineApplicationListener(OnlineApplicationListener listener);

}