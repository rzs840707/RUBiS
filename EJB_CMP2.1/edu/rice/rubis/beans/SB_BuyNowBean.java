package edu.rice.rubis.beans;

import java.rmi.RemoteException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;
import java.io.Serializable;
import javax.transaction.UserTransaction;

/**
 * This is a stateless session bean used to build the html form to buy an item.
 *  
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.1
 */

public class SB_BuyNowBean implements SessionBean 
{
  protected SessionContext sessionContext;
  protected Context initialContext = null;


  /**
   * Authenticate the user and get the information to build the html form.
   *
   * @return a string in html format
   * @since 1.1
   */
  public String getBuyNowForm(Integer itemId, String username, String password) throws RemoteException
  {
    UserLocal user = null;
    String html = "";

    // Authenticate the user who want to comment
      if ((username != null && !username.equals("")) || (password != null && !password.equals("")))
      {
        SB_AuthLocalHome authHome = null;
        SB_AuthLocal auth = null;
        try 
        {
          authHome = (SB_AuthLocalHome)initialContext.lookup("java:comp/env/ejb/SB_Auth");
          auth = authHome.create();
        } 
        catch (Exception e)
        {
          throw new RemoteException("Cannot lookup SB_Auth: " +e);
        }
        try 
        {
          user = auth.authenticate(username, password);
        } 
        catch (Exception e)
        {
          throw new RemoteException("Authentication failed: " +e);
        }
        if (user == null)
        {
           html = (" You don't have an account on RUBiS!<br>You have to register first.<br>");
           return html;
        }
      }
      // Try to find the Item corresponding to the Item ID
      ItemLocalHome itemHome;
      try 
      {
        itemHome = (ItemLocalHome)initialContext.lookup("java:comp/env/ejb/Item");
      } 
      catch (Exception e)
      {
        throw new RemoteException("Cannot lookup Item: " +e+"<br>");
      }
      try
      {
        ItemLocal item = itemHome.findByPrimaryKey(itemId);

        // Display the form for buying the item
        html = printItemDescriptionToBuyNow(item, user.getId());
      } 
      catch (Exception e) 
      {
        throw new RemoteException("Exception getting the item information: "+ e +"<br>");
      }
 
    return html;
  }
                   
  /**
   * Print the full description of an item and the buy now option
   *
   * @param item an <code>Item</code> value
   * @param userId an authenticated user id
   */
  public String printItemDescriptionToBuyNow(ItemLocal item, int userId) throws RemoteException
  {
    String html = "";
    try
    {
      String itemName = item.getName();
      html = html + "<TABLE width=\"100%\" bgcolor=\"#CCCCFF\">\n<TR><TD align=\"center\" width=\"100%\"><FONT size=\"4\" color=\"#000000\"><B>You are ready to buy this item: "+itemName+"</B></FONT></TD></TR>\n</TABLE><p>\n" + item.printItemDescriptionToBuyNow(userId);
      ;
    }
    catch (Exception re)
    {
      throw new EJBException("Unable to print Item description (exception: "+re+")<br>\n");
    }
    return html;
  }




  // ======================== EJB related methods ============================

  /**
   * This method is empty for a stateless session bean
   */
  public void ejbCreate() throws CreateException, RemoteException
  {
  }

  /** This method is empty for a stateless session bean */
  public void ejbActivate() throws RemoteException {}
  /** This method is empty for a stateless session bean */
  public void ejbPassivate() throws RemoteException {}
  /** This method is empty for a stateless session bean */
  public void ejbRemove() throws RemoteException {}


  /** 
   * Sets the associated session context. The container calls this method 
   * after the instance creation. This method is called with no transaction context. 
   * We also retrieve the Home interfaces of all RUBiS's beans.
   *
   * @param sessionContext - A SessionContext interface for the instance. 
   * @exception RemoteException - Thrown if the instance could not perform the function 
   *            requested by the container because of a system-level error. 
   */
  public void setSessionContext(SessionContext sessionContext) throws RemoteException
  {
    this.sessionContext = sessionContext;
    
    try
    {
      initialContext = new InitialContext(); 
    }
    catch (Exception e) 
    {
      throw new RemoteException("Cannot get JNDI InitialContext");
    }
  }

}
