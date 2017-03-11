package cube;

import ucb.gui2.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Observer;
import java.util.Observable;

import java.awt.event.MouseEvent;

/** Widget for displaying grid and cube.
 *  @author P. N. Hilfinger
 */
class CubeBoardWidget extends Pad implements Observer {

    /** Length of side of one square, in pixels. */
    static final int SQDIM = 50;
    /** Margin around the grid in pixels. */
    static final int MARGIN = 30;

    /** z-coordinate of projection plane. */
    static final double PROJECTION_Z = 2 * SQDIM;
    /** x-offset of projected board from "actual" one. */
    static final int X_OFFSET = SQDIM / 2;
    /** y-offset of projected board from "actual" one. */
    static final int Y_OFFSET = SQDIM / 2;

    /** Color of blank square or face. */
    private static final Color BLANK = Color.WHITE;
    /** Color of painted square or face. */
    private static final Color PAINTED = new Color(100, 100, 255);
    /** Color of painted lines. */
    private static final Color LINE = Color.BLACK;

    /** Model being displayed. */
    private static CubeModel _model;

    /** A new widget displaying MODEL. */
    CubeBoardWidget(CubeModel model) {
        _model = model;
        _previous = new CubeModel(model);
        _current = new CubeModel(model);
        setSize();
        setMouseHandler("click", this::readMove);
        _model.addObserver(this);
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        int n = _current.side();
        int D = SQDIM;
        g.setColor(BLANK);
        g.fillRect(0, 0, _dim, _dim);

        for (int r = 0; r < n; r += 1) {
            for (int c = 0; c < n; c += 1) {
                if (_current.isPaintedSquare(r, c)) {
                    g.setColor(PAINTED);
                } else {
                    g.setColor(BLANK);
                }
                int x0 = MARGIN - X_OFFSET + c * SQDIM;
                int y0 = MARGIN - Y_OFFSET + (n - r) * SQDIM;
                drawQuad(g, x0, y0, 0,
                         x0 + D, y0, 0,
                         x0 + D, y0 - D, 0,
                         x0, y0 - D, 0);
            }
        }

        int cx0 = MARGIN - X_OFFSET + _current.cubeCol() * SQDIM;
        int cy0 = MARGIN - Y_OFFSET + (n - _current.cubeRow()) * SQDIM;
        for (int f = 0; f < FACE_DIMS.length; f += 1) {
            int[] off = FACE_DIMS[f];
            if (off == null) {
                continue;
            }
            if (_current.isPaintedFace(f)) {
                g.setColor(PAINTED);
            } else {
                g.setColor(BLANK);
            }
            drawQuad(g, cx0 + off[0], cy0 + off[1], off[2],
                     cx0 + off[3], cy0 + off[4], off[5],
                     cx0 + off[6], cy0 + off[7], off[8],
                     cx0 + off[9], cy0 + off[10], off[11]);
        }
    }

    /** Return mouse's row at last click (may be out of range if mouse off
     *  the board). */
    int mouseRow() {
        return _mouseRow;
    }

    /** Return mouse's column at last click (may be out of range if mouse off
     *  the board). */
    int mouseCol() {
        return _mouseCol;
    }

    /** Record mouse's current position from click event WHERE. */
    private void readMove(String unused, MouseEvent where) {
        int x = where.getX(), y = where.getY();
        if (where.getButton() == MouseEvent.BUTTON1) {
            _mouseCol = (x - MARGIN) / SQDIM;
            _mouseRow = _current.side() - (y - MARGIN) / SQDIM - 1;
            setChanged();
            notifyObservers("click");
        }
    }

    /** Return projected x coordinate of point at (X, *, Z). */
    private int projx(int x, int z) {
        return (int) (x + (1 - z / PROJECTION_Z) * X_OFFSET);
    }

    /** Return projected y coordinate of point at (*, Y, Z). */
    private int projy(int y, int z) {
        return (int) (y + (1 - z / PROJECTION_Z) * Y_OFFSET);
    }

    /** Draw outlined and filled quadrilateral on G at coplanar points
     *  (P[0], P[1], P[2]), ..., (P[9], P[10], P[11]). */
    private void drawQuad(Graphics2D g, Integer... P) {
        int[] xpoints = new int[4];
        int[] ypoints = new int[4];

        for (int i = 0; i < 4; i += 1) {
            xpoints[i] = projx(P[i * 3], P[i * 3 + 2]);
            ypoints[i] = projy(P[i * 3 + 1], P[i * 3 + 2]);
        }
        g.fillPolygon(xpoints, ypoints, 4);
        g.setColor(LINE);
        g.drawPolygon(xpoints, ypoints, 4);
    }

    @Override
    public synchronized void update(Observable model, Object arg) {
        _previous.initialize(_current);
        _current.initialize(_model);
        if (_current.side() != _previous.side()) {
            setSize();
        }
        repaint();
    }

    /** Resize widget to current model size. */
    private void setSize() {
        int side = _current.side();
        _dim = side * SQDIM + 2 * MARGIN;
        setPreferredSize(_dim, _dim);
    }

    /** Local copies of cube model and previous model. */
    private CubeModel _current, _previous;

    /** Dimension of current drawing surface in pixels. */
    private int _dim;

    /** row and column of last mouse click (may be off the board). */
    private int _mouseRow, _mouseCol;

    /** Displacements of vertices on faces of cube relative to lower
     *  left corner of square of board.  Faces not displayed are null. */
    private static final int[][] FACE_DIMS = {
        { 0, 0, 0, SQDIM, 0, 0, SQDIM, 0, SQDIM, 0, 0, SQDIM },
        null,
        null,
        { SQDIM, 0, 0, SQDIM, -SQDIM, 0, SQDIM, -SQDIM, SQDIM, SQDIM,
          0, SQDIM },
        null,
        { 0, 0, SQDIM, SQDIM, 0, SQDIM, SQDIM, -SQDIM, SQDIM, 0,
          -SQDIM, SQDIM }
    };
}
