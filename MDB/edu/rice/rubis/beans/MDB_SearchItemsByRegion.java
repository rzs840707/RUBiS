package edu.rice.rubis.beans;

import java.rmi.RemoteException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.EJBException;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.Serializable;
import java.net.URLEncoder;

/**
 * This is a message driven bean used to get the list of items
 * that belong to a specific category in a specific region. 
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.1
 */

public class MDB_SearchItemsByRegion implements MessageDrivenBean, MessageListener 
{
  private DataSource dataSource;
  private MessageDrivenContext messageDrivenContext;
  private TopicConnectionFactory connectionFactory;
  private TopicConnection connection;
  private Topic topic;
  private TopicSession session;
  private TopicPublisher replier;
  private Context initialContext = null;

  public MDB_SearchItemsByRegion()
  {

  }

  public void onMessage(Message message)
  {
    try
    {
      MapMessage request = (MapMessage)message;
      String correlationID = request.getJMSCorrelationID();
      Integer categoryId = new Integer(request.getIntProperty("categoryId"));
      Integer regionId = new Integer(request.getIntProperty("regionId"));
      int page = request.getIntProperty("page");
      int nbOfItems = request.getIntProperty("nbOfItems");

        // Retrieve the connection factory
        connectionFactory = (TopicConnectionFactory) initialContext.lookup(BeanConfig.TopicConnectionFactoryName);

      // get the list of categories
      String html = getItems(categoryId, regionId, page, nbOfItems);

      // send the reply
      TemporaryTopic temporaryTopic = (TemporaryTopic) request.getJMSReplyTo();
      if (temporaryTopic != null)
      {
        // create a connection
        connection = connectionFactory.createTopicConnection();
        // create a session: no transaction, auto ack
        session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        TextMessage reply = session.createTextMessage();
        reply.setJMSCorrelationID(correlationID);
        reply.setText(html);
        replier = session.createPublisher(null); // unidentified publisher
        connection.start();
        replier.publish(temporaryTopic, reply);
        replier.close();
        session.close();
        connection.stop();
        connection.close();
      }
    }
    catch (Exception e)
    {
      throw new EJBException("Message traitment failed for MDB_SearchItemsByRegion: " +e);
    }
  }

  /**
   * Get the items in a specific category.
   *
   * @return a string that is the list of items in html format
   * @since 1.1
   */
  public String getItems(Integer categoryId, Integer regionId, int page, int nbOfItems) throws RemoteException
  {
    Connection        conn = null;
    PreparedStatement stmt = null;
    ResultSet rs           = null; 
    String itemName, endDate;
    int itemId;
    float maxBid, initialPrice;
    int nbOfBids=0;
    StringBuffer html = new StringBuffer();

    // get the list of items
    try
    {
      conn = dataSource.getConnection();
      stmt = conn.prepareStatement("SELECT items.name, items.id, items.end_date, items.max_bid, items.nb_of_bids, items.initial_price FROM items,users WHERE items.category=? AND items.seller=users.id AND users.region=? AND end_date>=NOW() LIMIT ?,?");
      stmt.setInt(1, categoryId.intValue());
      stmt.setInt(2, regionId.intValue());
      stmt.setInt(3, page*nbOfItems);
      stmt.setInt(4, nbOfItems);
      rs = stmt.executeQuery();

      stmt.close();
      conn.close();
    }
    catch (SQLException e)
    {
      try
      {
        if (stmt != null) stmt.close();
        if (conn != null) conn.close();
      }
      catch (Exception ignore)
      {
      }
      throw new RemoteException("Failed to get the items: " +e);
    }
    try 
    {
      while (rs.next()) 
      {
        itemName = rs.getString("name");
        itemId = rs.getInt("id");
        endDate = rs.getString("end_date");
        maxBid = rs.getFloat("max_bid");
        nbOfBids = rs.getInt("nb_of_bids");
        initialPrice = rs.getFloat("initial_price");
        if (maxBid <initialPrice)
          maxBid = initialPrice;
        html.append(printItem(itemName, itemId, maxBid, nbOfBids, endDate));
      }
    } 
    catch (Exception e)
    {
        throw new RemoteException("Cannot get items list: " +e);
    }
    return html.toString();
  }


  /**
   * Display item information as an HTML table row
   *
   * @return a <code>String</code> containing HTML code
   * @exception RemoteException if an error occurs
   * @since 1.0
   */
  public String printItem(String name, int id, float maxBid, int nbOfBids, String endDate) throws RemoteException
  {
    return "<TR><TD><a href=\"/servlet/edu.rice.rubis.beans.servlets.ViewItem?itemId="+id+"\">"+name+
      "<TD>"+maxBid+
      "<TD>"+nbOfBids+
      "<TD>"+endDate+
      "<TD><a href=\"/servlet/edu.rice.rubis.beans.servlets.PutBidAuth?itemId="+id+"\"><IMG SRC=\"/EJB_HTML/bid_now.jpg\" height=22 width=90></a>\n";
  }
  



  // ======================== EJB related methods ============================
 /** 
   * Set the associated context. The container call this method
   * after the instance creation. 
   * The enterprise Bean instance should store the reference to the context
   * object in an instance variable. 
   * This method is called with no transaction context.
   *
   * @param MessageDrivenContext A MessageDrivenContext interface for the instance.
   * @throws EJBException Thrown by the method to indicate a failure caused by
   * a system-level error.
   */
  public void setMessageDrivenContext(MessageDrivenContext ctx)
  {
    messageDrivenContext = ctx;
    if (dataSource == null)
    {
      // Finds DataSource from JNDI
      try
      {
        initialContext = new InitialContext(); 
        dataSource = (DataSource)initialContext.lookup("java:comp/env/jdbc/rubis");
      }
      catch (Exception e) 
      {
        throw new EJBException("Cannot get JNDI InitialContext");
      }
    }
  }

  /**
   * The Message driven  bean must define an ejbCreate methods with no args.
   *
   */
  public void ejbCreate() 
  {

  }
 
  /**
   * A container invokes this method before it ends the life of the message-driven object. 
   * This happens when a container decides to terminate the message-driven object. 
   *
   * This method is called with no transaction context. 
   *
   * @throws EJBException Thrown by the method to indicate a failure caused by
   * a system-level error.
   */
  public void ejbRemove() {}
 

}
