package cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages;
 		
 		// --- IMPORTS FROM /messages/settings/javasettings/javaimport BEGIN
			import java.util.*;import javax.vecmath.*;import cz.cuni.amis.pogamut.base.communication.messages.*;import cz.cuni.amis.pogamut.base.communication.worldview.*;import cz.cuni.amis.pogamut.base.communication.worldview.event.*;import cz.cuni.amis.pogamut.base.communication.worldview.object.*;import cz.cuni.amis.pogamut.multi.communication.worldview.object.*;import cz.cuni.amis.pogamut.base.communication.translator.event.*;import cz.cuni.amis.pogamut.multi.communication.translator.event.*;import cz.cuni.amis.pogamut.base3d.worldview.object.*;import cz.cuni.amis.pogamut.base3d.worldview.object.event.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.*;import cz.cuni.amis.pogamut.ut2004.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;import cz.cuni.amis.utils.exception.*;import cz.cuni.amis.pogamut.base.communication.translator.event.IWorldObjectUpdateResult.Result;import cz.cuni.amis.utils.SafeEquals;import cz.cuni.amis.pogamut.base.agent.*;import cz.cuni.amis.pogamut.multi.agent.*;import cz.cuni.amis.pogamut.multi.communication.worldview.property.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.property.*;import cz.cuni.amis.utils.token.*;import cz.cuni.amis.utils.*;
		// --- IMPORTS FROM /messages/settings/javasettings/javaimport END
		
		
		// --- IMPORTS FROM extra/code/java/javapart/classcategory[@name='all'] BEGIN
				
		// --- IMPORTS FROM extra/code/java/javapart/classcategory[@name='all'] END
		
		// --- IMPORTS FROM extra/code/java/javapart/classcategory[@name=static]+classtype[@name=impl] BEGIN
		
		// --- IMPORTS FROM extra/code/java/javapart/classcategory[@name=static]+classtype[@name=impl] END
    
 		/**
         *  
            				Implementation of the static part of the GameBots2004 message PRJ.  
            			
         *
         *  <p></p><p></p>
         *  Complete message documentation:               
         *  
		Both asynchronous and synchronous message. Incoming projectile that we can see.
	
         */
 	public class IncomingProjectileStaticImpl 
  						extends
  						IncomingProjectileStatic
	    {
 	
    	
    	
    	/**
    	 * Parameter-less contructor for the message.
    	 */
		public IncomingProjectileStaticImpl()
		{
		}
	
    	
    	
    	
    	/**
		 * Creates new instance of the message IncomingProjectile.
		 * 
		Both asynchronous and synchronous message. Incoming projectile that we can see.
	
		 * Corresponding GameBots message
		 *   (static part)
		 *   is
		 *   PRJ.
		 * 
 	  	 * 
		 *   
		 *     @param Id Unique Id of the projectile.
		 *   
		 * 
		 *   
		 * 
		 *   
		 * 
		 *   
		 * 
		 *   
		 * 
		 *   
		 * 
		 *   
		 * 
		 *   
		 * 
		 *   
		 * 
		 *   
		 * 
		 */
		public IncomingProjectileStaticImpl(
			UnrealId Id
		) {
			
					this.Id = Id;
				
		}
    
    	/**
		 * Cloning constructor from the full message.
		 *
		 * @param original
		 */
		public IncomingProjectileStaticImpl(IncomingProjectile original) {		
			
					this.Id = original.getId()
 	;
				
			this.SimTime = original.getSimTime();			
		}
		
		/**
		 * Cloning constructor from the full message.
		 *
		 * @param original
		 */
		public IncomingProjectileStaticImpl(IncomingProjectileStaticImpl original) {		
			
					this.Id = original.getId()
 	;
				
			this.SimTime = original.getSimTime();
		}
		
			/**
			 * Cloning constructor from the message part.
			 *
			 * @param original
			 */
			public IncomingProjectileStaticImpl(IncomingProjectileStatic original) {
				
						this.Id = original.getId()
 	;
					
			}
		
   				
   				@Override
   				public void setSimTime(long SimTime) {
					super.setSimTime(SimTime);
				}
   			
	    				@Override
	    				public 
	    				IncomingProjectileStaticImpl clone() {
	    					return new 
	    					IncomingProjectileStaticImpl(this);
	    				}
	    				
	    				
    	
	    /**
         * Unique Id of the projectile. 
         */
        protected
         UnrealId Id =
       	null;
	
 		/**
         * Unique Id of the projectile. 
         */
        public  UnrealId getId()
 	 {
				    					return Id;
				    				}
				    			
    	
    	
    	public static class IncomingProjectileStaticUpdate
     implements IStaticWorldObjectUpdatedEvent
		{
			
			private IncomingProjectileStatic data;
			private long time;
			
			public IncomingProjectileStaticUpdate
    (IncomingProjectileStatic source, long time)
			{
				this.data = source;
				this.time = time;
			}
			
			/**
			 * Simulation time in MILLI SECONDS !!!
			 */
			@Override
			public long getSimTime() {
				return time;
			}
	
			@Override
			public WorldObjectId getId() {
				return data.getId();
			}
			
			@Override
			public IWorldObjectUpdateResult<IStaticWorldObject> update(
					IStaticWorldObject object) {
				if ( object == null)
				{
					data = new IncomingProjectileStaticImpl(data);
					return new IWorldObjectUpdateResult.WorldObjectUpdateResult<IStaticWorldObject>(IWorldObjectUpdateResult.Result.CREATED, data);
				}
				//since static objects can't be updated, we'll just check that the object stays the same
				if ( object instanceof IncomingProjectileStaticImpl)
				{
					IncomingProjectileStaticImpl orig = (IncomingProjectileStaticImpl)object;
					//since these errors usually mean error in gamebots, we will just print an error message
					if ( data.isDifferentFrom(orig) )
					{
						//data.isDifferentFrom(orig);
						//throw new PogamutException("Trying to modify static object " + this.data.getId().toString() , this);
						System.out.println("!!!!!ERROR!!!!!! in static object modification. Object class : IncomingProjectileStaticImpl to see which property was different see !!!!PROPERTY UPDATE ERROR!!!!");
					}
					return new IWorldObjectUpdateResult.WorldObjectUpdateResult<IStaticWorldObject>(IWorldObjectUpdateResult.Result.SAME, data);
				}				
				throw new PogamutException("Unexpected object type for update, IncomingProjectileStaticImpl expected not class " + object.getClass().getSimpleName() + ".", this);
			}
		}
	
    
 		
 		@Override
 		public boolean isDifferentFrom(IStaticWorldObject other)
 		{
 			if (other == null) //early fail
 			{
 				return true;
 			}
 			else if (other == this) //early out
 			{
 				return false;
 			}
 			else
 			{
 				IncomingProjectileStatic obj = (IncomingProjectileStatic) other;

 				
 						if ( !(
 	 			AdvancedEquals.equalsOrNull(this.getId()
 	, obj.getId()
 	)
 	 		) )
						{
							System.out.println("!!!!!PROPERTY UPDATE ERROR!!!! on property Id on object class IncomingProjectileStatic");
							return true;
						}
 					
 			}
 			return false;
 		}
 	 
 		
 	    public String toString() {
            return
            	super.toString() + "[" +
            	
		              			"Id = " + String.valueOf(getId()
 	) + " | " + 
		              		
				"]";           		
        }
 	
 		
 		public String toHtmlString() {
 			return super.toString() + "[<br/>" +
            	
		              			"<b>Id</b> = " + String.valueOf(getId()
 	) + " <br/> " + 
		              		
				"<br/>]";     
		}
 	
 		
 		// --- Extra Java from XML BEGIN (extra/code/java/javapart/classcategory[@name=all]) ---
        	
		// --- Extra Java from XML END (extra/code/java/javapart/classcategory[@name=all]) ---
		
	    // --- Extra Java from XML BEGIN (extra/code/java/javapart/classcategory[@name=static+classtype[@name=impl]) ---
	        
	    // --- Extra Java from XML END (extra/code/java/javapart/classcategory[@name=static+classtype[@name=impl]) ---        	            	
 	
		}
 	