const fs = require("fs");
const path = require("path");
const eventBus = require("./eventBus");

// Audit log file path
const logFilePath = path.join(__dirname, "audit_logs.txt");

// Write logs asynchronously to text file
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
eventBus.on("log", (event, status, service, message) => {
    setImmediate(() => {
        writeLog(event, status, service, message);
    });
});

module.exports = writeLog;
