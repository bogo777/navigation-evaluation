
	 	/**
         IMPORTANT !!!

         DO NOT EDIT THIS FILE. IT IS GENERATED FROM approriate xml file in xmlresources/gbcommands BY
         THE JavaClassesGenerator.xslt. MODIFY THAT FILE INSTEAD OF THIS ONE.
         
         Use Ant task process-gb-messages after that to generate .java files again.
         
         IMPORTANT END !!!
        */
 	package cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands;import java.util.*;import javax.vecmath.*;import cz.cuni.amis.pogamut.base.communication.messages.*;import cz.cuni.amis.pogamut.base.communication.worldview.*;import cz.cuni.amis.pogamut.base.communication.worldview.event.*;import cz.cuni.amis.pogamut.base.communication.worldview.object.*;import cz.cuni.amis.pogamut.multi.communication.worldview.object.*;import cz.cuni.amis.pogamut.base.communication.translator.event.*;import cz.cuni.amis.pogamut.multi.communication.translator.event.*;import cz.cuni.amis.pogamut.base3d.worldview.object.*;import cz.cuni.amis.pogamut.base3d.worldview.object.event.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.*;import cz.cuni.amis.pogamut.ut2004.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.objects.*;import cz.cuni.amis.pogamut.ut2004.communication.translator.itemdescriptor.*;import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;import cz.cuni.amis.utils.exception.*;import cz.cuni.amis.pogamut.base.communication.translator.event.IWorldObjectUpdateResult.Result;import cz.cuni.amis.utils.SafeEquals;import cz.cuni.amis.pogamut.base.agent.*;import cz.cuni.amis.pogamut.multi.agent.*;import cz.cuni.amis.pogamut.multi.communication.worldview.property.*;import cz.cuni.amis.pogamut.ut2004multi.communication.worldview.property.*;import cz.cuni.amis.utils.token.*;import cz.cuni.amis.utils.*;import java.awt.*;import java.awt.geom.*;
 		/**
 		 * Representation of the GameBots2004 command DLGITEM.
 		 *
 		 * 
		Specifies properties of a new dialog component.
	
         */
 	public class DialogItem 
		extends CommandMessage
	        {
	        	
		        
    	/** Example how the message looks like - used during parser tests. */
    	public static final String PROTOTYPE =
    		" {Player text}  {DialogId text}  {Effect text}  {Type text}  {Id text}  {Parent text}  {Size null}  {Position null}  {Background text}  {BgColor null}  {Border null}  {Text text}  {TextColor null}  {Action text}  {Mode text}  {Checked text}  {NotChecked text}  {Image text}  {Origin null}  {Scale null} ";
    
		/**
		 * Creates new instance of command DialogItem.
		 * 
		Specifies properties of a new dialog component.
	
		 * Corresponding GameBots message for this command is
		 * DLGITEM.
		 *
		 * 
		 *    @param Player Name of the player on who's HUD should the dialog show.
		 *    @param DialogId Id of dialog, which should the message alter. If empty, actually constructed dialog is used.
		 *    @param Effect What should this message do. Available are: ADD, EDIT.
		 *    @param Type Type of the new component. Available are: PANEL, BUTTON, TEXT, IMAGE, OPTIONLIST, OPTION.
		 *    @param Id Identification of this component. It should be an unique alphanumeric string without spaces.
		 *    @param Parent Id of parent component. If left empty, the parent is dialog itself. Parent must be specified before the child.
		 *    @param Size Applicable for: PANEL, BUTTON, TEXT, OPTIONLIST, IMAGE. A pair of values in form "Width,Height" where each is in range from 0 to 1 (relative portion of the parent component).
		 *    @param Position Applicable for: PANEL, BUTTON, TEXT, OPTIONLIST, IMAGE. A pair of values in form "Left,Top" where each is in range from 0 to 1 (relative portion of the parent component).
		 *    @param Background Applicable for: PANEL, BUTTON. Name of texture, which should be used as background.
		 *    @param BgColor Applicable for: PANEL, BUTTON. RGBA color of background.
		 *    @param Border Applicable for: PANEL, BUTTON. Triple of numbers from 0 to 255 separated by commas, specifying the RGB color of border.
		 *    @param Text Applicable for: TEXT, BUTTON, OPTION. Text which should be written on the component.
		 *    @param TextColor Applicable for: TEXT, BUTTON, OPTION. Color of text which should be written on the component as RGB triple of numbers from 0 to 255 separated by commas.
		 *    @param Action Applicable for: BUTTON, IMAGE. Action which should be performed when the button is pressed. SUBMIT submits data from the dialog using DLGCMD message with Command SUBMIT, RESET resets the dialog and all other values send DLGCMD message with this value as Command - without closing the dialog.
		 *    @param Mode Applicable for: OPTIONLIST. Either SINGLE - just one option can be selected at one moment (radio buttons), or MULTI - classic check boxes.
		 *    @param Checked Applicable for: OPTION. Name of texture which should be used when the button is checked.
		 *    @param NotChecked Applicable for: OPTION. Name of texture which should be used when the button is not checked.
		 *    @param Image Applicable for: IMAGE. Name of texture of the image.
		 *    @param Origin Applicable for: IMAGE. Left top origin point of the image.
		 *    @param Scale Applicable for: IMAGE. Left top origin point of the image.
		 */
		public DialogItem(
			String Player,  String DialogId,  String Effect,  String Type,  String Id,  String Parent,  Dimension2D Size,  Point2D Position,  String Background,  Color BgColor,  Color Border,  String Text,  Color TextColor,  String Action,  String Mode,  String Checked,  String NotChecked,  String Image,  Point2D Origin,  Dimension2D Scale
		) {
			
				this.Player = Player;
            
				this.DialogId = DialogId;
            
				this.Effect = Effect;
            
				this.Type = Type;
            
				this.Id = Id;
            
				this.Parent = Parent;
            
				this.Size = Size;
            
				this.Position = Position;
            
				this.Background = Background;
            
				this.BgColor = BgColor;
            
				this.Border = Border;
            
				this.Text = Text;
            
				this.TextColor = TextColor;
            
				this.Action = Action;
            
				this.Mode = Mode;
            
				this.Checked = Checked;
            
				this.NotChecked = NotChecked;
            
				this.Image = Image;
            
				this.Origin = Origin;
            
				this.Scale = Scale;
            
		}

		
			/**
			 * Creates new instance of command DialogItem.
			 * 
		Specifies properties of a new dialog component.
	
			 * Corresponding GameBots message for this command is
			 * DLGITEM.
			 * <p></p>
			 * WARNING: this is empty-command constructor, you have to use setters to fill it up with data that should be sent to GameBots2004!
		     */
		    public DialogItem() {
		    }
			
		
		/**
		 * Cloning constructor.
		 *
		 * @param original
		 */
		public DialogItem(DialogItem original) {
		   
		        this.Player = original.Player;
		   
		        this.DialogId = original.DialogId;
		   
		        this.Effect = original.Effect;
		   
		        this.Type = original.Type;
		   
		        this.Id = original.Id;
		   
		        this.Parent = original.Parent;
		   
		        this.Size = original.Size;
		   
		        this.Position = original.Position;
		   
		        this.Background = original.Background;
		   
		        this.BgColor = original.BgColor;
		   
		        this.Border = original.Border;
		   
		        this.Text = original.Text;
		   
		        this.TextColor = original.TextColor;
		   
		        this.Action = original.Action;
		   
		        this.Mode = original.Mode;
		   
		        this.Checked = original.Checked;
		   
		        this.NotChecked = original.NotChecked;
		   
		        this.Image = original.Image;
		   
		        this.Origin = original.Origin;
		   
		        this.Scale = original.Scale;
		   
		}
    
	        /**
	        Name of the player on who's HUD should the dialog show. 
	        */
	        protected
	         String Player =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Name of the player on who's HUD should the dialog show. 
         */
        public String getPlayer()
 	
	        {
	            return
	        	 Player;
	        }
	        
	        
	        
 		
 		/**
         * Name of the player on who's HUD should the dialog show. 
         */
        public DialogItem 
        setPlayer(String Player)
 	
			{
				this.Player = Player;
				return this;
			}
		
	        /**
	        Id of dialog, which should the message alter. If empty, actually constructed dialog is used. 
	        */
	        protected
	         String DialogId =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Id of dialog, which should the message alter. If empty, actually constructed dialog is used. 
         */
        public String getDialogId()
 	
	        {
	            return
	        	 DialogId;
	        }
	        
	        
	        
 		
 		/**
         * Id of dialog, which should the message alter. If empty, actually constructed dialog is used. 
         */
        public DialogItem 
        setDialogId(String DialogId)
 	
			{
				this.DialogId = DialogId;
				return this;
			}
		
	        /**
	        What should this message do. Available are: ADD, EDIT. 
	        */
	        protected
	         String Effect =
	       	"ADD";
	
	        
	        
 		/**
         * What should this message do. Available are: ADD, EDIT. 
         */
        public String getEffect()
 	
	        {
	            return
	        	 Effect;
	        }
	        
	        
	        
 		
 		/**
         * What should this message do. Available are: ADD, EDIT. 
         */
        public DialogItem 
        setEffect(String Effect)
 	
			{
				this.Effect = Effect;
				return this;
			}
		
	        /**
	        Type of the new component. Available are: PANEL, BUTTON, TEXT, IMAGE, OPTIONLIST, OPTION. 
	        */
	        protected
	         String Type =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Type of the new component. Available are: PANEL, BUTTON, TEXT, IMAGE, OPTIONLIST, OPTION. 
         */
        public String getType()
 	
	        {
	            return
	        	 Type;
	        }
	        
	        
	        
 		
 		/**
         * Type of the new component. Available are: PANEL, BUTTON, TEXT, IMAGE, OPTIONLIST, OPTION. 
         */
        public DialogItem 
        setType(String Type)
 	
			{
				this.Type = Type;
				return this;
			}
		
	        /**
	        Identification of this component. It should be an unique alphanumeric string without spaces. 
	        */
	        protected
	         String Id =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Identification of this component. It should be an unique alphanumeric string without spaces. 
         */
        public String getId()
 	
	        {
	            return
	        	 Id;
	        }
	        
	        
	        
 		
 		/**
         * Identification of this component. It should be an unique alphanumeric string without spaces. 
         */
        public DialogItem 
        setId(String Id)
 	
			{
				this.Id = Id;
				return this;
			}
		
	        /**
	        Id of parent component. If left empty, the parent is dialog itself. Parent must be specified before the child. 
	        */
	        protected
	         String Parent =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Id of parent component. If left empty, the parent is dialog itself. Parent must be specified before the child. 
         */
        public String getParent()
 	
	        {
	            return
	        	 Parent;
	        }
	        
	        
	        
 		
 		/**
         * Id of parent component. If left empty, the parent is dialog itself. Parent must be specified before the child. 
         */
        public DialogItem 
        setParent(String Parent)
 	
			{
				this.Parent = Parent;
				return this;
			}
		
	        /**
	        Applicable for: PANEL, BUTTON, TEXT, OPTIONLIST, IMAGE. A pair of values in form "Width,Height" where each is in range from 0 to 1 (relative portion of the parent component). 
	        */
	        protected
	         Dimension2D Size =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: PANEL, BUTTON, TEXT, OPTIONLIST, IMAGE. A pair of values in form "Width,Height" where each is in range from 0 to 1 (relative portion of the parent component). 
         */
        public Dimension2D getSize()
 	
	        {
	            return
	        	 Size;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: PANEL, BUTTON, TEXT, OPTIONLIST, IMAGE. A pair of values in form "Width,Height" where each is in range from 0 to 1 (relative portion of the parent component). 
         */
        public DialogItem 
        setSize(Dimension2D Size)
 	
			{
				this.Size = Size;
				return this;
			}
		
	        /**
	        Applicable for: PANEL, BUTTON, TEXT, OPTIONLIST, IMAGE. A pair of values in form "Left,Top" where each is in range from 0 to 1 (relative portion of the parent component). 
	        */
	        protected
	         Point2D Position =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: PANEL, BUTTON, TEXT, OPTIONLIST, IMAGE. A pair of values in form "Left,Top" where each is in range from 0 to 1 (relative portion of the parent component). 
         */
        public Point2D getPosition()
 	
	        {
	            return
	        	 Position;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: PANEL, BUTTON, TEXT, OPTIONLIST, IMAGE. A pair of values in form "Left,Top" where each is in range from 0 to 1 (relative portion of the parent component). 
         */
        public DialogItem 
        setPosition(Point2D Position)
 	
			{
				this.Position = Position;
				return this;
			}
		
	        /**
	        Applicable for: PANEL, BUTTON. Name of texture, which should be used as background. 
	        */
	        protected
	         String Background =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: PANEL, BUTTON. Name of texture, which should be used as background. 
         */
        public String getBackground()
 	
	        {
	            return
	        	 Background;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: PANEL, BUTTON. Name of texture, which should be used as background. 
         */
        public DialogItem 
        setBackground(String Background)
 	
			{
				this.Background = Background;
				return this;
			}
		
	        /**
	        Applicable for: PANEL, BUTTON. RGBA color of background. 
	        */
	        protected
	         Color BgColor =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: PANEL, BUTTON. RGBA color of background. 
         */
        public Color getBgColor()
 	
	        {
	            return
	        	 BgColor;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: PANEL, BUTTON. RGBA color of background. 
         */
        public DialogItem 
        setBgColor(Color BgColor)
 	
			{
				this.BgColor = BgColor;
				return this;
			}
		
	        /**
	        Applicable for: PANEL, BUTTON. Triple of numbers from 0 to 255 separated by commas, specifying the RGB color of border. 
	        */
	        protected
	         Color Border =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: PANEL, BUTTON. Triple of numbers from 0 to 255 separated by commas, specifying the RGB color of border. 
         */
        public Color getBorder()
 	
	        {
	            return
	        	 Border;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: PANEL, BUTTON. Triple of numbers from 0 to 255 separated by commas, specifying the RGB color of border. 
         */
        public DialogItem 
        setBorder(Color Border)
 	
			{
				this.Border = Border;
				return this;
			}
		
	        /**
	        Applicable for: TEXT, BUTTON, OPTION. Text which should be written on the component. 
	        */
	        protected
	         String Text =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: TEXT, BUTTON, OPTION. Text which should be written on the component. 
         */
        public String getText()
 	
	        {
	            return
	        	 Text;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: TEXT, BUTTON, OPTION. Text which should be written on the component. 
         */
        public DialogItem 
        setText(String Text)
 	
			{
				this.Text = Text;
				return this;
			}
		
	        /**
	        Applicable for: TEXT, BUTTON, OPTION. Color of text which should be written on the component as RGB triple of numbers from 0 to 255 separated by commas. 
	        */
	        protected
	         Color TextColor =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: TEXT, BUTTON, OPTION. Color of text which should be written on the component as RGB triple of numbers from 0 to 255 separated by commas. 
         */
        public Color getTextColor()
 	
	        {
	            return
	        	 TextColor;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: TEXT, BUTTON, OPTION. Color of text which should be written on the component as RGB triple of numbers from 0 to 255 separated by commas. 
         */
        public DialogItem 
        setTextColor(Color TextColor)
 	
			{
				this.TextColor = TextColor;
				return this;
			}
		
	        /**
	        Applicable for: BUTTON, IMAGE. Action which should be performed when the button is pressed. SUBMIT submits data from the dialog using DLGCMD message with Command SUBMIT, RESET resets the dialog and all other values send DLGCMD message with this value as Command - without closing the dialog. 
	        */
	        protected
	         String Action =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: BUTTON, IMAGE. Action which should be performed when the button is pressed. SUBMIT submits data from the dialog using DLGCMD message with Command SUBMIT, RESET resets the dialog and all other values send DLGCMD message with this value as Command - without closing the dialog. 
         */
        public String getAction()
 	
	        {
	            return
	        	 Action;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: BUTTON, IMAGE. Action which should be performed when the button is pressed. SUBMIT submits data from the dialog using DLGCMD message with Command SUBMIT, RESET resets the dialog and all other values send DLGCMD message with this value as Command - without closing the dialog. 
         */
        public DialogItem 
        setAction(String Action)
 	
			{
				this.Action = Action;
				return this;
			}
		
	        /**
	        Applicable for: OPTIONLIST. Either SINGLE - just one option can be selected at one moment (radio buttons), or MULTI - classic check boxes. 
	        */
	        protected
	         String Mode =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: OPTIONLIST. Either SINGLE - just one option can be selected at one moment (radio buttons), or MULTI - classic check boxes. 
         */
        public String getMode()
 	
	        {
	            return
	        	 Mode;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: OPTIONLIST. Either SINGLE - just one option can be selected at one moment (radio buttons), or MULTI - classic check boxes. 
         */
        public DialogItem 
        setMode(String Mode)
 	
			{
				this.Mode = Mode;
				return this;
			}
		
	        /**
	        Applicable for: OPTION. Name of texture which should be used when the button is checked. 
	        */
	        protected
	         String Checked =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: OPTION. Name of texture which should be used when the button is checked. 
         */
        public String getChecked()
 	
	        {
	            return
	        	 Checked;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: OPTION. Name of texture which should be used when the button is checked. 
         */
        public DialogItem 
        setChecked(String Checked)
 	
			{
				this.Checked = Checked;
				return this;
			}
		
	        /**
	        Applicable for: OPTION. Name of texture which should be used when the button is not checked. 
	        */
	        protected
	         String NotChecked =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: OPTION. Name of texture which should be used when the button is not checked. 
         */
        public String getNotChecked()
 	
	        {
	            return
	        	 NotChecked;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: OPTION. Name of texture which should be used when the button is not checked. 
         */
        public DialogItem 
        setNotChecked(String NotChecked)
 	
			{
				this.NotChecked = NotChecked;
				return this;
			}
		
	        /**
	        Applicable for: IMAGE. Name of texture of the image. 
	        */
	        protected
	         String Image =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: IMAGE. Name of texture of the image. 
         */
        public String getImage()
 	
	        {
	            return
	        	 Image;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: IMAGE. Name of texture of the image. 
         */
        public DialogItem 
        setImage(String Image)
 	
			{
				this.Image = Image;
				return this;
			}
		
	        /**
	        Applicable for: IMAGE. Left top origin point of the image. 
	        */
	        protected
	         Point2D Origin =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: IMAGE. Left top origin point of the image. 
         */
        public Point2D getOrigin()
 	
	        {
	            return
	        	 Origin;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: IMAGE. Left top origin point of the image. 
         */
        public DialogItem 
        setOrigin(Point2D Origin)
 	
			{
				this.Origin = Origin;
				return this;
			}
		
	        /**
	        Applicable for: IMAGE. Left top origin point of the image. 
	        */
	        protected
	         Dimension2D Scale =
	       	
	        		null
	        	;
	
	        
	        
 		/**
         * Applicable for: IMAGE. Left top origin point of the image. 
         */
        public Dimension2D getScale()
 	
	        {
	            return
	        	 Scale;
	        }
	        
	        
	        
 		
 		/**
         * Applicable for: IMAGE. Left top origin point of the image. 
         */
        public DialogItem 
        setScale(Dimension2D Scale)
 	
			{
				this.Scale = Scale;
				return this;
			}
		
 	    public String toString() {
            return toMessage();
        }
 	
 		public String toHtmlString() {
			return super.toString() + "[<br/>" +
            	
            	"<b>Player</b> = " +
            	String.valueOf(getPlayer()
 	) +
            	" <br/> " +
            	
            	"<b>DialogId</b> = " +
            	String.valueOf(getDialogId()
 	) +
            	" <br/> " +
            	
            	"<b>Effect</b> = " +
            	String.valueOf(getEffect()
 	) +
            	" <br/> " +
            	
            	"<b>Type</b> = " +
            	String.valueOf(getType()
 	) +
            	" <br/> " +
            	
            	"<b>Id</b> = " +
            	String.valueOf(getId()
 	) +
            	" <br/> " +
            	
            	"<b>Parent</b> = " +
            	String.valueOf(getParent()
 	) +
            	" <br/> " +
            	
            	"<b>Size</b> = " +
            	String.valueOf(getSize()
 	) +
            	" <br/> " +
            	
            	"<b>Position</b> = " +
            	String.valueOf(getPosition()
 	) +
            	" <br/> " +
            	
            	"<b>Background</b> = " +
            	String.valueOf(getBackground()
 	) +
            	" <br/> " +
            	
            	"<b>BgColor</b> = " +
            	String.valueOf(getBgColor()
 	) +
            	" <br/> " +
            	
            	"<b>Border</b> = " +
            	String.valueOf(getBorder()
 	) +
            	" <br/> " +
            	
            	"<b>Text</b> = " +
            	String.valueOf(getText()
 	) +
            	" <br/> " +
            	
            	"<b>TextColor</b> = " +
            	String.valueOf(getTextColor()
 	) +
            	" <br/> " +
            	
            	"<b>Action</b> = " +
            	String.valueOf(getAction()
 	) +
            	" <br/> " +
            	
            	"<b>Mode</b> = " +
            	String.valueOf(getMode()
 	) +
            	" <br/> " +
            	
            	"<b>Checked</b> = " +
            	String.valueOf(getChecked()
 	) +
            	" <br/> " +
            	
            	"<b>NotChecked</b> = " +
            	String.valueOf(getNotChecked()
 	) +
            	" <br/> " +
            	
            	"<b>Image</b> = " +
            	String.valueOf(getImage()
 	) +
            	" <br/> " +
            	
            	"<b>Origin</b> = " +
            	String.valueOf(getOrigin()
 	) +
            	" <br/> " +
            	
            	"<b>Scale</b> = " +
            	String.valueOf(getScale()
 	) +
            	" <br/> " +
            	 
            	"<br/>]"
            ;
		}
 	
		public String toMessage() {
     		StringBuffer buf = new StringBuffer();
     		buf.append("DLGITEM");
     		
						if (Player != null) {
							buf.append(" {Player " + Player + "}");
						}
					
						if (DialogId != null) {
							buf.append(" {DialogId " + DialogId + "}");
						}
					
						if (Effect != null) {
							buf.append(" {Effect " + Effect + "}");
						}
					
						if (Type != null) {
							buf.append(" {Type " + Type + "}");
						}
					
						if (Id != null) {
							buf.append(" {Id " + Id + "}");
						}
					
						if (Parent != null) {
							buf.append(" {Parent " + Parent + "}");
						}
					
					    if (Size != null) {
					        buf.append(" {Size " +
					            Size.getWidth() + "," +
					            Size.getHeight() + "}");
					    }
					
					    if (Position != null) {
					        buf.append(" {Position " +
					            Position.getX() + "," +
					            Position.getY() + "}");
					    }
					
						if (Background != null) {
							buf.append(" {Background " + Background + "}");
						}
					
					    if (BgColor != null) {
					        buf.append(" {BgColor " +
					            BgColor.getRed() + "," +
					            BgColor.getGreen() + "," +
					            BgColor.getBlue() + "," +
					            BgColor.getAlpha() + "}");
					    }
					
					    if (Border != null) {
					        buf.append(" {Border " +
					            Border.getRed() + "," +
					            Border.getGreen() + "," +
					            Border.getBlue() + "," +
					            Border.getAlpha() + "}");
					    }
					
						if (Text != null) {
							buf.append(" {Text " + Text + "}");
						}
					
					    if (TextColor != null) {
					        buf.append(" {TextColor " +
					            TextColor.getRed() + "," +
					            TextColor.getGreen() + "," +
					            TextColor.getBlue() + "," +
					            TextColor.getAlpha() + "}");
					    }
					
						if (Action != null) {
							buf.append(" {Action " + Action + "}");
						}
					
						if (Mode != null) {
							buf.append(" {Mode " + Mode + "}");
						}
					
						if (Checked != null) {
							buf.append(" {Checked " + Checked + "}");
						}
					
						if (NotChecked != null) {
							buf.append(" {NotChecked " + NotChecked + "}");
						}
					
						if (Image != null) {
							buf.append(" {Image " + Image + "}");
						}
					
					    if (Origin != null) {
					        buf.append(" {Origin " +
					            Origin.getX() + "," +
					            Origin.getY() + "}");
					    }
					
					    if (Scale != null) {
					        buf.append(" {Scale " +
					            Scale.getWidth() + "," +
					            Scale.getHeight() + "}");
					    }
					
   			return buf.toString();
   		}
 	
 		// --- Extra Java from XML BEGIN (extra/code/java)
        	
				        
				        
			      
		// --- Extra Java from XML END (extra/code/java)
 	
	        }
    	