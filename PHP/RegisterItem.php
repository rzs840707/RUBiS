<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <body>
    <?php
    $scriptName = "RegisterItem.php";
    require "PHPprinter.php";
    $startTime = getMicroTime();

    $userId = $_POST['userId'];
    if ($userId == null)
    {
      $userId = $_GET['userId'];
      if ($userId == null)
      {
         printError($scriptName, $startTime, "RegisterItem", "<h3>You must provide a user identifier!<br></h3>");
         exit();
      }
    }

    $categoryId = $_POST['categoryId'];
    if ($categoryId == null)
    {
      $categoryId = $_GET['categoryId'];
      if ($categoryId == null)
      {
         printError($scriptName, $startTime, "RegisterItem", "<h3>You must provide a category identifier !<br></h3>");
         exit();
      }
    }

    $name = $_POST['name'];
    if ($name == null)
    {
      $name = $_GET['name'];
      if ($name == null)
      {
         printError($scriptName, $startTime, "RegisterItem", "<h3>You must provide an item name !<br></h3>");
         exit();
      }
    }

    $initialPrice = $_POST['initialPrice'];
    if ($initialPrice == null)
    {
      $initialPrice = $_GET['initialPrice'];
      if ($initialPrice == null)
      {
         printError($scriptName, $startTime, "RegisterItem", "<h3>You must provide an initial price !<br></h3>");
         exit();
      }
    }

    $reservePrice = $_POST['reservePrice'];
    if ($reservePrice == null)
    {
      $reservePrice = $_GET['reservePrice'];
      if ($reservePrice == null)
      {
         printError($scriptName, $startTime, "RegisterItem", "<h3>You must provide a reserve price !<br></h3>");
         exit();
      }
    }

    $buyNow = $_POST['buyNow'];
    if ($buyNow == null)
    {
      $buyNow = $_GET['buyNow'];
      if ($buyNow == null)
      {
         printError($scriptName, $startTime, "RegisterItem", "<h3>You must provide a Buy Now price !<br></h3>");
         exit();
      }
    }

    $duration = $_POST['duration'];
    if ($duration == null)
    {
      $duration = $_GET['duration'];
      if ($duration == null)
      {
         printError($scriptName, $startTime, "RegisterItem", "<h3>You must provide a duration !<br></h3>");
         exit();
      }
    }

    $qty = $_POST['quantity'];
    if ($qty == null)
    {
      $qty = $_GET['quantity'];
      if ($qty == null)
      {
         printError($scriptName, $startTime, "RegisterItem", "<h3>You must provide a quantity !<br></h3>");
         exit();
      }
    }

    $description = $_POST['description'];
    if ($description == null)
    {
      $description = $_GET['description'];
      if ($description == null)
        $description = "No description";
    }

    getDatabaseLink($link);

    // Add item to database
    $start = date("Y:m:d H:i:s");
    $end = date("Y:m:d H:i:s", mktime(date("H"), date("i"),date("s"), date("m"), date("d")+$duration, date("Y")));
    $result = mysql_query("INSERT INTO items VALUES (NULL, \"$name\", \"$description\", $initialPrice, $qty, $reservePrice, $buyNow, 0, 0, '$start', '$end', $userId, $categoryId)", $link) or die("ERROR: Failed to insert new item in database. MySQL reports '".mysql_error()."' while querying 'INSERT INTO items VALUES (NULL, \"$name\", \"$description\", $initialPrice, $quantity, $reservePrice, $buyNow, '$start', '$end', $userId, $categoryId)'");

    printHTMLheader("RUBiS: Selling $name");
    print("<center><h2>Your Item has been successfully registered.</h2></center><br>\n");
    print("<b>RUBiS has stored the following information about your item:</b><br><p>\n");
    print("<TABLE>\n");
    print("<TR><TD>Name<TD>$name\n");
    print("<TR><TD>Description<TD>$description\n");
    print("<TR><TD>Initial price<TD>$initialPrice\n");
    print("<TR><TD>ReservePrice<TD>$reservePrice\n");
    print("<TR><TD>Buy Now<TD>$buyNow\n");
    print("<TR><TD>Quantity<TD>$qty\n");
    print("<TR><TD>Duration<TD>$duration\n");
    print("</TABLE>\n");
    print("<br><b>The following information has been automatically generated by RUBiS:</b><br>\n");
    print("<TABLE>\n");
    print("<TR><TD>User id<TD>$userId\n");
    print("<TR><TD>Category id<TD>$categoryId\n");
    print("</TABLE>\n");

    mysql_close($link);

    printHTMLfooter($scriptName, $startTime);
    ?>
  </body>
</html>
