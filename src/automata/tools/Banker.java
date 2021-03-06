package com.sqweebloid.jane.automata.tools;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.someclient.api.Item;
import org.someclient.api.Query;
import org.someclient.api.queries.BankItemQuery;
import org.someclient.api.queries.WidgetItemQuery;
import org.someclient.api.widgets.Widget;
import org.someclient.api.widgets.WidgetInfo;
import org.someclient.api.widgets.WidgetItem;
import org.someclient.client.util.QueryRunner;

import com.sqweebloid.jane.automata.Automaton;
import com.sqweebloid.jane.automata.tools.movement.MoveGraph;

/**
 * Deals with all things banking.
 */
public class Banker extends Automaton {
    enum Bank {
        VARROCK_WEST(MoveGraph.Node.VARROCK_WEST_BANK),
        ALKARID(MoveGraph.Node.ALKARID_BANK);

        MoveGraph.Node location;

        Bank(MoveGraph.Node node) {
            location = node;
        }

        MoveGraph.Node getNode() {
            return location;
        }
    }

    private final int[] BOOTH_ID = new int[]{6943, 7409};

    private static final String WITHDRAW = "Withdraw";
    private static final String DEPOSIT = "Deposit";

    private boolean justOpen = false;

    private int slot;

    // Into or out from bank
    // true = deposit
    // false = withdraw
    private boolean into = false;

    // If this is one, we just click the item.
    // If it's greater than one, we withdraw/deposit-x.
    // If it's -1 we withdraw all.
    private int amount = 1;

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void setInto(boolean into) {
        this.into = into;
    }

    public void setJustOpen(boolean open) {
        this.justOpen = open;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setAll() {
        this.amount = -1;
    }

    public boolean isBoothOpen() {
        Widget box = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        return box != null && !box.isHidden();
    }

    public MoveGraph.Node getNearestBank() {
        List<MoveGraph.Node> banks = new ArrayList();

        for (Bank bank : Bank.values()) {
            banks.add(bank.getNode());
        }

        // This is a bad approximation.
        Collections.sort(banks, (one, other) ->
                getWorldLocation().distanceTo(one.getLocation()) -
                getWorldLocation().distanceTo(other.getLocation()));

        return banks.get(0);
    }

    @Override
    public void run() {
        MoveGraph.Node closest = getNearestBank();
        int distance = closest.getLocation().distanceTo(getWorldLocation());

        if (!isBoothOpen()) {
            if (distance > 8) go(closest);
            object(BOOTH_ID).closest().interact("Bank");
            sleep().most();
        }

        if (justOpen) return;

        String action = into ? DEPOSIT : WITHDRAW;
        Item[] slots = into ?
            inventory.getInventoryItems() :
            inventory.getBankItems();

        if (slot >= slots.length) {
            logger.error("Slot {} bigger than {}", slot, slots.length);
            return;
        }

        Item target = slots[slot];

        List<WidgetItem> widgets = into ?
            inventory.getInventoryWidgets() :
            inventory.getBankWidgets();

        Optional<WidgetItem> targetWidget = widgets
            .stream()
            .filter(widget ->
                    widget.getId() == target.getId() &&
                    widget.getQuantity() == target.getQuantity())
            .findFirst();

        if (!targetWidget.isPresent()) {
            logger.error("Mismatch between slot and widgets (id={} quantity={})", target.getId(), target.getQuantity());
            return;
        }

        Rectangle bounds = targetWidget.get().getCanvasBounds();

        if (bounds == null) {
            logger.error("Item's widget had null bounds");
            return;
        }

        switch (amount) {
            case 1:
                mouse(bounds).left();
                break;
            case -1:
                mouse(bounds).right();
                sleep().most();
                menu(action + "-All").done();
                break;
            default:
                mouse(bounds).right();
                sleep().more();
                menu(action + "-X").done();
                sleep().most();
                type(String.valueOf(amount));
                input.typeKey(KeyEvent.VK_ENTER);
                break;
        }
    }
}
