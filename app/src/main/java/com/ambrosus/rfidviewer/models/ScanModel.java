package com.ambrosus.rfidviewer.models;

import android.content.Context;
import android.util.Log;

import com.uk.tsl.rfid.asciiprotocol.commands.BarcodeCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.FactoryDefaultsCommand;
import com.uk.tsl.rfid.asciiprotocol.commands.InventoryCommand;
import com.uk.tsl.rfid.asciiprotocol.enumerations.TriState;
import com.uk.tsl.rfid.asciiprotocol.responders.IBarcodeReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ICommandResponseLifecycleDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.ITransponderReceivedDelegate;
import com.uk.tsl.rfid.asciiprotocol.responders.TransponderData;
import com.uk.tsl.utils.HexEncoding;

import java.util.Locale;

public class ScanModel extends ModelBase{

    private Context context;
    private boolean enabled;
    private boolean anyTagSeen;

    // The command to use as a responder to capture incoming inventory responses
    private InventoryCommand inventoryResponder;
    // The command used to issue commands
    private InventoryCommand inventoryCommand;
    // The command to use as a responder to capture incoming barcode responses
    private BarcodeCommand barcodeResponder;

    public ScanModel(Context context){
        this.context = context;

        // This is the command that will be used to perform configuration changes and inventories
        inventoryCommand = new InventoryCommand();
        inventoryCommand.setResetParameters(TriState.YES);
        // Configure the type of inventory
        inventoryCommand.setIncludeTransponderRssi(TriState.NO);
        inventoryCommand.setIncludeChecksum(TriState.NO);
        inventoryCommand.setIncludePC(TriState.NO);
        inventoryCommand.setIncludeDateTime(TriState.YES);

        // Use an InventoryCommand as a responder to capture all incoming inventory responses
        inventoryResponder = new InventoryCommand();
        // Also capture the responses that were not from App commands
        inventoryResponder.setCaptureNonLibraryResponses(true);
        // Notify when each transponder is seen
        inventoryResponder.setTransponderReceivedDelegate(new ITransponderReceivedDelegate() {

            int mTagsSeen = 0;
            @Override
            public void transponderReceived(TransponderData transponder, boolean moreAvailable) {
                anyTagSeen = true;
                sendMessageNotification("EPC:" + transponder.getEpc());
                mTagsSeen++;
                if( !moreAvailable) {
                    sendMessageNotification("");
                    Log.d("TagCount",String.format("Tags seen: %s", mTagsSeen));
                }
            }
        });

        inventoryResponder.setResponseLifecycleDelegate( new ICommandResponseLifecycleDelegate() {

            @Override
            public void responseEnded() {
                if( !anyTagSeen && inventoryCommand.getTakeNoAction() != TriState.YES) {
                    sendMessageNotification("No transponders seen");
                }
                inventoryCommand.setTakeNoAction(TriState.NO);
            }

            @Override
            public void responseBegan() {
                anyTagSeen = false;
            }
        });

        // This command is used to capture barcode responses
        barcodeResponder = new BarcodeCommand();
        barcodeResponder.setCaptureNonLibraryResponses(true);
        barcodeResponder.setUseEscapeCharacter(TriState.YES);
        barcodeResponder.setBarcodeReceivedDelegate(new IBarcodeReceivedDelegate() {
            @Override
            public void barcodeReceived(String barcode) {
                sendMessageNotification("BC: " + barcode);
            }
        });
    }

    public void setEnabled(boolean state)
    {
        boolean oldState = enabled;
        enabled = state;

        // Update the commander for state changes
        if(oldState != state) {
            if( enabled ) {
                // Listen for transponders
                getCommander().addResponder(inventoryResponder);
                // Listen for barcodes
                getCommander().addResponder(barcodeResponder);
            } else {
                // Stop listening for transponders
                getCommander().removeResponder(inventoryResponder);
                // Stop listening for barcodes
                getCommander().removeResponder(barcodeResponder);
            }

        }
    }

    public InventoryCommand getCommand() { return inventoryCommand; }

    //
    // Reset the reader configuration to default command values
    //
    public void resetDevice()
    {
        if(getCommander().isConnected()) {
            getCommander().executeCommand(new FactoryDefaultsCommand());
        }
    }

    //
    // Update the reader configuration from the command
    // Call this after each change to the model's command
    //
    public void updateConfiguration()
    {
        if(getCommander().isConnected()) {
            inventoryCommand.setTakeNoAction(TriState.YES);
            getCommander().executeCommand(inventoryCommand);
        }
    }

    //
    // Perform an inventory scan with the current command parameters
    //
    public void scan()
    {
        testForAntenna();
        if(getCommander().isConnected()) {
            inventoryCommand.setTakeNoAction(TriState.NO);
            getCommander().executeCommand(inventoryCommand);
        }
    }


    //
    // Test for the presence of the antenna
    //
    public void testForAntenna()
    {
        if(getCommander().isConnected()) {
            InventoryCommand testCommand = InventoryCommand.synchronousCommand();
            testCommand.setTakeNoAction(TriState.YES);
            getCommander().executeCommand(testCommand);
            if( !testCommand.isSuccessful() ) {
                sendMessageNotification("ER:Error! Code: " + testCommand.getErrorCode() + " " + testCommand.getMessages().toString());
            }
        }
    }

}
