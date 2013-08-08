package com.ib.modeloTabla;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;

public class Modelo<T extends ConvierteAVector> extends AbstractTableModel {

    private List<T> data;
    protected List<T> allData;
    protected List<String> columns = new ArrayList<String>();
    protected boolean primeraVez = true;
    protected static final List<ConvierteAVector> emptyList = new ArrayList<ConvierteAVector>();
    protected static final List<String> emptyColumns = new ArrayList<String>();

    protected Modelo() {
        primeraVez = true;
    }

    protected Modelo(List<T> datos) {
        this();
        if (datos != null) {
            if (datos.isEmpty()) {
                setColumnas(emptyColumns);
            } else {
                setColumnas(datos.get(0).getTitulos());
            }
            setDatos(datos);
            setTodos(datos);
        }
    }

    /**
     * Creates a new TableSorter object with the given data and sets it to the
     * JTable
     *
     * @param <T>
     * @param datos
     * @param table
     * @return
     * @throws IllegalArgumentException if the parameter table==null
     */
    public static <T extends ConvierteAVector> void crearModelo(List<T> datos, JTable table) {
        if (table == null) {
            throw new IllegalArgumentException("The given jTable has not been initialized");
        }
        if (table.getModel() != null && table.getModel() instanceof TableSorter) {
            int currentColumnCount = table.getColumnCount();
            ListSelectionModel lsm = table.getSelectionModel();
            int firstSelectedRow = lsm.getMinSelectionIndex();
            int lastSelectedRow = lsm.getMaxSelectionIndex();
            TableSorter sorter = (TableSorter) table.getModel();
            Modelo model = (Modelo) sorter.getTableModel();
            if (datos == null || datos.isEmpty()) {
                model.setDatos(emptyList);
                model.setTodos(emptyList);
                model.setColumnas(emptyColumns);
            } else {
                model.setDatos(datos);
                model.setTodos(datos);
                model.setColumnas(datos.get(0).getTitulos());
            }
            int newColumnCount = model.getColumnCount();
            if (currentColumnCount != newColumnCount) {
                model.fireTableStructureChanged();
            } else {
                model.fireTableDataChanged();
                if (!model.getDatos().isEmpty()) {
                    if (lastSelectedRow < model.getDatos().size()) {
                        lsm.setSelectionInterval(firstSelectedRow, lastSelectedRow);
                    } else {
                        lsm.setSelectionInterval(model.getDatos().size() - 1, model.getDatos().size() - 1);
                    }
                }
            }
            if (table.getColumnCount() > 1) {//TODO: CORREGIR ESTO PARA SIMPLIFICAR Y NO REPETIR CODIGO
                autoResizeColWidth(table);
            } else {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            }
        } else {
            Modelo<? extends ConvierteAVector> m;
            if (datos != null) {
                m = new Modelo<T>(datos);
            } else {
                m = new Modelo<ConvierteAVector>(emptyList);
            }
            TableSorter tableSorter = new TableSorter(m, table.getTableHeader());
            table.setModel(tableSorter);
            if (table.getColumnCount() > 1) {
                autoResizeColWidth(table);
            } else {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            }
        }
    }

    /**
     * Creates a new TableModel (no sorter) object with the given data and sets
     * it to the JTable
     *
     * @param <T>
     * @param datos
     * @param table
     * @return
     * @throws IllegalArgumentException if the parameter table==null
     */
    public static <T extends ConvierteAVector> void crearModeloSinSorter(List<T> datos, JTable table) {
        if (table == null) {
            throw new IllegalArgumentException("The given jTable has not been initialized");
        }
        Modelo<? extends ConvierteAVector> m;
        if (datos != null) {
            m = new Modelo<T>(datos);
        } else {
            m = new Modelo<ConvierteAVector>(emptyList);
        }
        table.setModel(m);
        if (table.getColumnCount() > 1) {
            autoResizeColWidth(table);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }
    }

    /**
     * Automatically adjusts the table's colums size to the data lenght
     *
     * @param table
     */
    public static void autoResizeColWidth(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        Component parent = table.getParent();
        double parentWidhtWorkAround = 0;
        if (parent.getParent() != null) {
            parent = parent.getParent();
            parentWidhtWorkAround = -2;
        }
        if (parent instanceof JScrollPane) {
            JScrollPane pane = (JScrollPane) parent;
            Border border = pane.getBorder();
            if (border != null && border instanceof TitledBorder) {
                parentWidhtWorkAround = -10;
            }
        }

        double tableWidth = parentWidhtWorkAround + Math.max(parent.getPreferredSize().getWidth(), parent.getWidth());
        double usedWidth = 0;
        double margin = 5;
        DefaultTableColumnModel colModel = (DefaultTableColumnModel) table.getColumnModel();
        for (int columnIndex = 0; columnIndex < table.getColumnCount(); columnIndex++) {
            TableColumn column = colModel.getColumn(columnIndex);
            double width = 0;
            TableCellRenderer renderer = column.getHeaderRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, 0);
            width = comp.getPreferredSize().getWidth();
            // Get maximum width of column data
            for (int r = 0; r < table.getRowCount(); r++) {
                renderer = table.getCellRenderer(r, columnIndex);
                comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, columnIndex), false, false, r, columnIndex);
                width = Math.max(width, comp.getPreferredSize().getWidth());
            }
            width += 2 * margin;
            column.setPreferredWidth((int) width);
            usedWidth += width;
        }
        double restar = 0;
        if (parent.getPreferredSize().getHeight() < (table.getRowCount() * table.getRowHeight())) {
            restar = 20;
        }
        int restante = (int) ((tableWidth - usedWidth - restar) / table.getColumnCount());
        if (restante > 0) {
            for (int i = 0; i < table.getColumnCount(); i++) {
                TableColumn col = colModel.getColumn(i);

                col.setPreferredWidth(col.getPreferredWidth() + restante);
                usedWidth += restante;
            }
        }
    }

    @Override
    public int getRowCount() {
        if (getDatos() == null) {
            return 0;
        } else {
            return getDatos().size();
        }
    }

    @Override
    public int getColumnCount() {
        if (getDatos() == null || getDatos().isEmpty()) {
            return 0;
        } else {
            return getDatos().get(0).getTitulos().size();
        }
    }

    @Override
    public void setValueAt(Object valor, int row, int col) {
        this.getDatos().get(row).setValueAt(col, valor);
        fireTableCellUpdated(row, col);
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (getDatos().get(row).getDatos().size() != getDatos().get(row).getTitulos().size()) {
            throw new IllegalArgumentException("Lists datos and titulos have different sizes for row: " + row + ", check getDatos and getTitulos implemented methods");
        }
        return getDatos().get(row).getDatos().get(column);
    }

    @Override
    public Class getColumnClass(int c) {
        Object o = getValueAt(0, c);
        if (o == null) {
            return String.class;
        } else {
            return getValueAt(0, c).getClass();
        }
    }

    @Override
    public String getColumnName(int col) {
        if (getColumnas() == null || getColumnas().isEmpty()) {
            return "";
        } else {
            return getColumnas().get(col);
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        try {
            Object dato = getValueAt(row, col);
            this.getDatos().get(row).setValueAt(col, dato);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Modifies the original model to display only the values that matches the
     * filter (Does not affect the data)
     *
     * @param filtro The text to use as filter
     * @param col The column to check for the condition
     * @param modelo Table's model (use table.getModel() method)
     */
    public static void filtrar(String filtro, int col, TableModel modelo) {
        TableSorter ts = (TableSorter) modelo;
        Modelo<ConvierteAVector> m = (Modelo) ts.getTableModel();
        if (filtro == null || filtro.equals("")) {
            removerFiltro(m);
        } else {
            if (m.isPrimeraVez()) {
                m.setPrimeraVez(false);
                m.setTodos(m.getDatos());
            }
            m.setDatos(new ArrayList<ConvierteAVector>());
            for (Iterator<ConvierteAVector> it = m.getTodos().iterator(); it.hasNext();) {
                ConvierteAVector uno = it.next();
                String dato = String.valueOf(uno.getDatos().get(col));
                dato = dato.toLowerCase();
                filtro = filtro.toLowerCase();
                if (dato.contains(filtro)) {
                    m.getDatos().add(uno);
                }
            }
            m.fireTableDataChanged();
        }
    }

    /**
     * Modifies the original model to display only the values that matches more
     * than one filter (Does not affect the data)
     *
     * @param filters An array of text to use as filter
     * @param columns An array of columns to check for the condition
     * @param modelo Table's model (use table.getModel() method)
     */
    public static void filtrar(String[] filters, int[] columns, TableModel modelo) {
        TableSorter ts = (TableSorter) modelo;
        Modelo<ConvierteAVector> m = (Modelo) ts.getTableModel();
        if (filters == null || filters.length == 0) {
            removerFiltro(m);
        } else {
            if (filters.length != columns.length) {
                throw new IllegalArgumentException("Filters and columns vectors have different sizes");
            }
            if (m.isPrimeraVez()) {
                m.setPrimeraVez(false);
                m.setTodos(m.getDatos());
            }
            m.setDatos(new ArrayList<ConvierteAVector>());
            for (Iterator<ConvierteAVector> it = m.getTodos().iterator(); it.hasNext();) {
                ConvierteAVector uno = it.next();
                boolean include = true;
                String[] data = new String[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    int j = columns[i];
                    data[i] = String.valueOf(uno.getDatos().get(j)).toLowerCase();
                    filters[i] = filters[i].toLowerCase();
                    if (filters[i] != null && !filters[i].isEmpty() && !data[i].contains(filters[i])) {
                        include = false;
                    }
                }
                if (include) {
                    m.getDatos().add(uno);
                }
            }
            m.fireTableDataChanged();
        }
    }

    /**
     * Removes the filter and displays the original data
     *
     * @param modelo
     */
    private static void removerFiltro(AbstractTableModel modelo) {
        Modelo<ConvierteAVector> m = (Modelo) modelo;
        m.setPrimeraVez(true);
        m.setDatos(m.getTodos());
        m.fireTableDataChanged();
    }

    public List<String> getColumnas() {
        return this.columns;
    }

    public final void setColumnas(List<String> columnas) {
        this.columns = columnas;
    }

    public boolean isPrimeraVez() {
        return primeraVez;
    }

    public void setPrimeraVez(boolean primeraVez) {
        this.primeraVez = primeraVez;
    }

    public List<T> getTodos() {
        return allData;
    }

    public final void setTodos(List<T> todos) {
        this.allData = todos;
    }

    /**
     * @return the datos
     */
    public List<T> getDatos() {
        return data;
    }

    /**
     * @param datos the datos to set
     */
    public final void setDatos(List<T> datos) {
        this.data = datos;
    }
}
