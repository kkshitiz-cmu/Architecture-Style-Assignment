
/******************************************************************************************************************
* File:REST.js
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2018 Carnegie Mellon University
* Versions:
*   1.0 February 2018 - Initial write of assignment 3 for 2018 architectures course(ajl).
*
* Description: This module provides the restful webservices for the Server.js Node server. This module contains GET,
* and POST services.  
*
* Parameters: 
*   router - this is the URL from the client
*   connection - this is the connection to the database
*   md5 - This is the md5 hashing/parser... included by convention, but not really used 
*
* Internal Methods: 
*   router.get("/"... - returns the system version information
*   router.get("/orders"... - returns a listing of everything in the ws_orderinfo database
*   router.get("/orders/:order_id"... - returns the data associated with order_id
*   router.post("/order?"... - adds the new customer data into the ws_orderinfo database
*
* External Dependencies: mysql
*
******************************************************************************************************************/
require("./logger"); // Start logging service
var mysql   = require("mysql");     //Database
const eventBus = require("./eventBus"); // Pubsub
var authenticateToken = require("./auth.js"); // Import the authenticateToken function

function REST_ROUTER(router,connection) {
    var self = this;
    self.handleRoutes(router,connection);
}

// Here is where we define the routes. Essentially a route is a path taken through the code dependent upon the 
// contents of the URL

REST_ROUTER.prototype.handleRoutes= function(router,connection) {

    // GET with no specifier - returns system version information
    // req paramdter is the request object
    // res parameter is the response object

    router.get("/",function(req,res){
        res.json({"Message":"Orders Webservices Server Version 1.0"});
    });
    
    // GET for /orders specifier - returns all orders currently stored in the database
    // req paramdter is the request object
    // res parameter is the response object

    // User Login with Token Generation
    router.post("/login", function (req, res) {
        const { userId, password } = req.body;

        if (!userId || !password) {
            eventBus.emit("log", "No user ID or password given", "ERROR", "AUTHENTICATION");
            return res.send("null");
        }

        // Check userId and password
        const query = "SELECT * FROM users WHERE user_id = ? AND password = ?";
        const values = [userId, password];

        connection.query(query, values, function (err, results) {
            if (err) {
                eventBus.emit("log", "Error connecting to database", "ERROR", "AUTHENTICATION", `${err}`);
                console.log(err);
                res.send("null");
                return;
            }

            if (results.length === 0) {
                eventBus.emit("log", "Error authenticating user", "ERROR", "AUTHENTICATION");
                res.send("null");
                return;
            }

            // Generate a random token
            const token = Math.random().toString(36).slice(2, 18); 

            // Store the token in the database
            var updateQuery = "UPDATE ?? SET ?? = ? WHERE ?? = ?";
            var updateTable = ["users", "token", token, "user_id", userId];
            updateQuery = mysql.format(updateQuery, updateTable);

            connection.query(updateQuery, function (err,rows) {
                if (err) {
                    eventBus.emit("log", "Error authenticating user", "ERROR", "AUTHENTICATION", `${err}`);
                    console.log(err);
                    return res.send("null");
                } else {
                    eventBus.emit("log", "Successfully authenticated user", "SUCCESS", "AUTHENTICATION");
                    return res.send(token);
                }
            });
        });
    });

    router.post("/logout", function(req,res){
        if (!authenticateToken(req, res, connection)) {
            return;
        }

        console.log("Logging the user out..." );
        const token = req.headers['authorization']; // Get token from request
        var query = "UPDATE ?? SET ?? = ? WHERE ?? = ?";
        var updateTable = ["users", "token", null, "token", token];
        var table = ["users"];
        query = mysql.format(query,table);
        connection.query(query,function(err,rows){
            if(err) {
                res.json({"Error" : true, "Message" : "Error executing MySQL query"});
            } else {
                res.json({"Error" : false, "Message" : "Success", "Orders" : rows});
            }
        });
    });

    router.get("/orders", function(req,res){
        if (!authenticateToken(req, res, connection)) {
            return;
        }

        var query = "SELECT * FROM ??";
        var table = ["orders"];
        query = mysql.format(query,table);
        connection.query(query,function(err,rows){
            if(err) {
                eventBus.emit("log", "Error fetching orders", "ERROR", "REST API", req.ip);
                res.json({"Error" : true, "Message" : "Error executing MySQL query"});
            } else {
                eventBus.emit("log", `Successfully retrieved ${rows.length} orders`, "SUCCESS", "REST API", req.ip);
                res.json({"Error" : false, "Message" : "Success", "Orders" : rows});
            }
        });
    });

    // GET for /orders/order id specifier - returns the order for the provided order ID
    // req paramdter is the request object
    // res parameter is the response object
     
    router.get("/orders/:order_id", function(req,res){
        if (!authenticateToken(req, res, connection)) {
            return;
        }

        console.log("Getting order ID: ", req.params.order_id );
        var query = "SELECT * FROM ?? WHERE ??=?";
        var table = ["orders","order_id",req.params.order_id];
        query = mysql.format(query,table);
        connection.query(query,function(err,rows){
            if(err) {
                eventBus.emit("log", "Error fetching order details", "ERROR", "REST API", req.ip);
                res.json({"Error" : true, "Message" : "Error executing MySQL query"});
            } else {
                eventBus.emit("log", `Successfully retrieved order details`, "SUCCESS", "REST API", req.ip);
                res.json({"Error" : false, "Message" : "Success", "Users" : rows});
            }
        });
    });

    // POST for /orders?order_date&first_name&last_name&address&phone - adds order
    // req paramdter is the request object - note to get parameters (eg. stuff afer the '?') you must use req.body.param
    // res parameter is the response object 
  
    router.post("/orders", function(req,res){
        if (!authenticateToken(req, res, connection)) {
            return;
        }

        //console.log("url:", req.url);
        //console.log("body:", req.body);
        console.log("Adding to orders table ", req.body.order_date,",",req.body.first_name,",",req.body.last_name,",",req.body.address,",",req.body.phone);
        var query = "INSERT INTO ??(??,??,??,??,??) VALUES (?,?,?,?,?)";
        var table = ["orders","order_date","first_name","last_name","address","phone",req.body.order_date,req.body.first_name,req.body.last_name,req.body.address,req.body.phone];
        query = mysql.format(query,table);
        connection.query(query,function(err,rows){
            if(err) {
                eventBus.emit("log", "Error creating order", "ERROR", "REST API", req.ip);
                res.json({"Error" : true, "Message" : "Error executing MySQL query"});
            } else {
                eventBus.emit("log", `Successfully created order`, "SUCCESS", "REST API", req.ip);
                res.json({"Error" : false, "Message" : "User Added !"});
            }
        });
    });

    //deletes order
    router.delete("/orders/:order_id", function(req,res){
        if (!authenticateToken(req, res, connection)) {
            return;
        }
        
        console.log("Deleting order ID: ", req.params.order_id );
        var query = "DELETE FROM ?? WHERE ??=?";
        var table = ["orders","order_id",req.params.order_id];
        query = mysql.format(query,table);
        connection.query(query,function(err,rows){
            if(err) {
                eventBus.emit("log", "Error deleting order", "ERROR", "REST API", req.ip);
                res.json({"Error" : true, "Message" : "Error executing MySQL query"});
            } else {
                if(rows.affectedRows > 0) {
                    eventBus.emit("log", "Successfully deleted order", "SUCCESS", "REST API", req.ip);
                    res.json({"Error" : false, "Message" : "Order deleted successfully"});
                } else {
                    eventBus.emit("log", "Error deleting order - order not found", "ERROR", "REST API", req.ip);
                    res.json({"Error" : false, "Message" : "No order found with this ID"});
                }
            }
        });
    });

}

// The next line just makes this module available... think of it as a kind package statement in Java

module.exports = REST_ROUTER;