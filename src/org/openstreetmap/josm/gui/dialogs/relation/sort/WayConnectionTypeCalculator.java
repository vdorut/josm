// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.gui.dialogs.relation.sort;

import static org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType.Direction.BACKWARD;
import static org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType.Direction.FORWARD;
import static org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType.Direction.NONE;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.dialogs.relation.sort.WayConnectionType.Direction;

public class WayConnectionTypeCalculator {

    private static final int UNCONNECTED = Integer.MIN_VALUE;

    private List<RelationMember> members;

    /**
     * refresh the cache of member WayConnectionTypes
     * @param members relation members
     * @return way connections
     */
    public List<WayConnectionType> updateLinks(List<RelationMember> members) {
        this.members = members;
        final List<WayConnectionType> con = new ArrayList<>();

        for (int i = 0; i < members.size(); ++i) {
            con.add(null);
        }

        firstGroupIdx = 0;

        lastForwardWay = UNCONNECTED;
        lastBackwardWay = UNCONNECTED;
        onewayBeginning = false;
        WayConnectionType lastWct = null;

        for (int i = 0; i < members.size(); ++i) {
            final RelationMember m = members.get(i);
            if (!m.isWay() || m.getWay() == null || m.getWay().isIncomplete()) {
                if (i > 0) {
                    makeLoopIfNeeded(con, i-1);
                }
                con.set(i, new WayConnectionType());
                firstGroupIdx = i;
                continue;
            }

            WayConnectionType wct = new WayConnectionType(false);
            wct.linkPrev = i > 0 && con.get(i-1) != null && con.get(i-1).isValid();
            wct.direction = NONE;

            if (RelationSortUtils.isOneway(m)) {
                if (lastWct != null && lastWct.isOnewayTail) {
                    wct.isOnewayHead = true;
                }
                if (lastBackwardWay == UNCONNECTED && lastForwardWay == UNCONNECTED) { //Beginning of new oneway
                    wct.isOnewayHead = true;
                    lastForwardWay = i-1;
                    lastBackwardWay = i-1;
                    onewayBeginning = true;
                }
            }

            if (wct.linkPrev) {
                if (lastBackwardWay != UNCONNECTED && lastForwardWay != UNCONNECTED) {
                    determineOnewayConnectionType(con, m, i, wct);
                    if (!wct.linkPrev) {
                        firstGroupIdx = i;
                    }
                }

                if (!RelationSortUtils.isOneway(m) && lastWct != null) {
                    wct.direction = determineDirection(i-1, lastWct.direction, i);
                    wct.linkPrev = wct.direction != NONE;
                }
            }

            if (!wct.linkPrev) {
                wct.direction = determineDirectionOfFirst(i, m);
                if (RelationSortUtils.isOneway(m)) {
                    wct.isOnewayLoopForwardPart = true;
                    lastForwardWay = i;
                }
            }

            wct.linkNext = false;
            if (lastWct != null) {
                lastWct.linkNext = wct.linkPrev;
            }
            con.set(i, wct);
            lastWct = wct;

            if (!wct.linkPrev) {
                if (i > 0) {
                    makeLoopIfNeeded(con, i-1);
                }
                firstGroupIdx = i;
            }
        }
        makeLoopIfNeeded(con, members.size()-1);

        return con;
    }

    private int firstGroupIdx;
    private void makeLoopIfNeeded(final List<WayConnectionType> con, final int i) {
        boolean loop;
        if (i == firstGroupIdx) { //is primitive loop
            loop = determineDirection(i, FORWARD, i) == FORWARD;
        } else {
            loop = determineDirection(i, con.get(i).direction, firstGroupIdx) == con.get(firstGroupIdx).direction;
        }
        if (loop) {
            for (int j = firstGroupIdx; j <= i; ++j) {
                con.get(j).isLoop = true;
            }
        }
    }

    private Direction determineDirectionOfFirst(final int i, final RelationMember m) {
        Direction result = RelationSortUtils.roundaboutType(m);
        if (result != NONE)
            return result;

        if (RelationSortUtils.isOneway(m)) {
            if (RelationSortUtils.isBackward(m)) return BACKWARD;
            else return FORWARD;
        } else { /** guess the direction and see if it fits with the next member */
            if (determineDirection(i, FORWARD, i+1) != NONE) return FORWARD;
            if (determineDirection(i, BACKWARD, i+1) != NONE) return BACKWARD;
        }
        return NONE;
    }

    private int lastForwardWay;
    private int lastBackwardWay;
    private boolean onewayBeginning;

    private void determineOnewayConnectionType(final List<WayConnectionType> con,
            RelationMember m, int i, final WayConnectionType wct) {
        Direction dirFW = determineDirection(lastForwardWay, con.get(lastForwardWay).direction, i);
        Direction dirBW;
        if (onewayBeginning) {
            if (lastBackwardWay < 0) {
                dirBW = determineDirection(firstGroupIdx, reverse(con.get(firstGroupIdx).direction), i, true);
            } else {
                dirBW = determineDirection(lastBackwardWay, con.get(lastBackwardWay).direction, i, true);
            }

            if (dirBW != NONE) {
                onewayBeginning = false;
            }
        } else {
            dirBW = determineDirection(lastBackwardWay, con.get(lastBackwardWay).direction, i, true);
        }

        if (RelationSortUtils.isOneway(m)) {
            if (dirBW != NONE) {
                wct.direction = dirBW;
                lastBackwardWay = i;
                wct.isOnewayLoopBackwardPart = true;
            }
            if (dirFW != NONE) {
                wct.direction = dirFW;
                lastForwardWay = i;
                wct.isOnewayLoopForwardPart = true;
            }
            // Not connected to previous
            if (dirFW == NONE && dirBW == NONE) {
                wct.linkPrev = false;
                if (RelationSortUtils.isOneway(m)) {
                    wct.isOnewayHead = true;
                    lastForwardWay = i-1;
                    lastBackwardWay = i-1;
                } else {
                    lastForwardWay = UNCONNECTED;
                    lastBackwardWay = UNCONNECTED;
                }
                onewayBeginning = true;
            }

            if (dirFW != NONE && dirBW != NONE) { //End of oneway loop
                if (i+1 < members.size() && determineDirection(i, dirFW, i+1) != NONE) {
                    wct.isOnewayLoopBackwardPart = false;
                    wct.direction = dirFW;
                } else {
                    wct.isOnewayLoopForwardPart = false;
                    wct.direction = dirBW;
                }

                wct.isOnewayTail = true;
            }

        } else {
            lastForwardWay = UNCONNECTED;
            lastBackwardWay = UNCONNECTED;
            if (dirFW == NONE || dirBW == NONE) {
                wct.linkPrev = false;
            }
        }
    }

    private static Direction reverse(final Direction dir) {
        if (dir == FORWARD) return BACKWARD;
        if (dir == BACKWARD) return FORWARD;
        return dir;
    }

    private Direction determineDirection(int refI, Direction refDirection, int k) {
        return determineDirection(refI, refDirection, k, false);
    }

    /**
     * Determines the direction of way {@code k} with respect to the way {@code ref_i}.
     * The way {@code ref_i} is assumed to have the direction {@code ref_direction} and to be the predecessor of {@code k}.
     *
     * If both ways are not linked in any way, NONE is returned.
     *
     * Else the direction is given as follows:
     * Let the relation be a route of oneway streets, and someone travels them in the given order.
     * Direction is FORWARD if it is legal and BACKWARD if it is illegal to do so for the given way.
     * @param refI way key
     * @param refDirection direction of ref_i
     * @param k successor of ref_i
     * @param reversed if {@code true} determine reverse direction
     * @return direction of way {@code k}
     */
    private Direction determineDirection(int refI, final Direction refDirection, int k, boolean reversed) {
        if (members == null || refI < 0 || k < 0 || refI >= members.size() || k >= members.size() || refDirection == NONE)
            return NONE;

        final RelationMember mRef = members.get(refI);
        final RelationMember m = members.get(k);
        Way wayRef = null;
        Way way = null;

        if (mRef.isWay()) {
            wayRef = mRef.getWay();
        }
        if (m.isWay()) {
            way = m.getWay();
        }

        if (wayRef == null || way == null)
            return NONE;

        /** the list of nodes the way k can dock to */
        List<Node> refNodes = new ArrayList<>();

        switch (refDirection) {
        case FORWARD:
            refNodes.add(wayRef.lastNode());
            break;
        case BACKWARD:
            refNodes.add(wayRef.firstNode());
            break;
        case ROUNDABOUT_LEFT:
        case ROUNDABOUT_RIGHT:
            refNodes = wayRef.getNodes();
            break;
        default: // Do nothing
        }

        for (Node n : refNodes) {
            if (n == null) {
                continue;
            }
            if (RelationSortUtils.roundaboutType(members.get(k)) != NONE) {
                for (Node nn : way.getNodes()) {
                    if (n == nn)
                        return RelationSortUtils.roundaboutType(members.get(k));
                }
            } else if (RelationSortUtils.isOneway(m)) {
                if (n == RelationNodeMap.firstOnewayNode(m) && !reversed) {
                    if (RelationSortUtils.isBackward(m))
                        return BACKWARD;
                    else
                        return FORWARD;
                }
                if (n == RelationNodeMap.lastOnewayNode(m) && reversed) {
                    if (RelationSortUtils.isBackward(m))
                        return FORWARD;
                    else
                        return BACKWARD;
                }
            } else {
                if (n == way.firstNode())
                    return FORWARD;
                if (n == way.lastNode())
                    return BACKWARD;
            }
        }
        return NONE;
    }
}
