package eu.fbk.iv4xr.mbt.efsm.labRecruits.levelGenerator;

/*
 * @author wish
 */
/*
 * changes are made to complain Java 8 for using evosuite 1.0.6
 */
public class Door {
	String ID ;
	boolean initialState = false ; // by default closed
	public Door(String id) { this.ID = id ; }
	private boolean hasFire = false;
	
	public void operatedBy(Button ... buttons) {
		//for(var B : buttons) {
		for(Button B : buttons) {
			B.associatedDoors.add(this) ;
		}
	}
	
	public void setFire() {
		this.hasFire = true;
	}
	
	public boolean hasFire() {
		return hasFire;
	}
	
}
