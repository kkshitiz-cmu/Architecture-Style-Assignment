const fs = require("fs");
const path = require("path");
const eventBus = require("./eventBus");

// Define log file path
const logFilePath = path.join(__dirname, "server_logs.txt");

// Function to write logs asynchronously
function writeLog(message) {
    const timestamp = new Date().toISOString();
    const logMessage = `[${timestamp}] ${message}\n`;
    
    fs.appendFile(logFilePath, logMessage, (err) => {
        if (err) {
            console.error("Failed to write log:", err);
        }
    });
}


// Subscribe to 'log' events asynchronously
eventBus.on("log", (event, status, service, user) => {
    console.log(`📥 Received log event: ${event}, ${status}, ${service}, ${user}`);
    setImmediate(() => { // Non-blocking execution
        writeLog(event, status, service, user);
    });
});

module.exports = writeLog;
