// auth.js
var mysql   = require("mysql");

function authenticateToken(req, res, connection) {
    const token = req.headers['authorization']; // Get token from request
    if (!token) {
        res.status(403).json({ "Error": true, "Message": "No token provided" });
        return false;
    }

    // Query the database to verify the token
    var query = "SELECT * FROM ?? WHERE ?? = ?";
    var table = ["users", "token", token];
    query = mysql.format(query, table);

    connection.query(query, function(err, rows) {
        if (err) {
            res.status(500).json({ "Error": true, "Message": "Error executing MySQL query" });
            return false;
        }
        if (rows.length === 0) {
            res.status(403).json({ "Error": true, "Message": "Invalid token" });
            return false;
        }

        // Token is valid, proceed to next middleware or route handler
        req.user = rows[0];  // Optionally attach user information to the request object
    });

    return true;
}

module.exports = authenticateToken;
