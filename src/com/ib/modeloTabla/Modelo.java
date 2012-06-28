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

    private List<T> datos;
    protected List<T> todos;
    protected List<String> columnas = new ArrayList<String>();
    protected boolean primeraVez = true;
    protected static ConvierteAVector datosNulos = new ConvierteAVector() {

        @Override
        public List<Object> getDatos() {
            List<Object> lista = new ArrayList<Object>();
            lista.add("No se encontraron datos");
            return lista;
        }

        @Override
        public ArrayList<String> getTitulos() {
            ArrayList<String> lista = new ArrayList<String>();
            lista.add("No se encontraron datos");
            return lista;
        }

        @Override
        public void setValueAt(int posicion, Object value) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    protected static List<ConvierteAVector> listaVacia = new ArrayList<ConvierteAVector>();
    protected boolean cellEditable = false;

    protected Modelo() {
        primeraVez = true;
    }

    protected Modelo(List<T> datos) {
        this();
        if (datos != null && datos.size() > 0) {
            setColumnas(datos.get(0).getTitulos());
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
                listaVacia = new ArrayList<ConvierteAVector>();
                listaVacia.add(datosNulos);
                model.setDatos(listaVacia);
                model.setTodos(listaVacia);
                model.setColumnas(listaVacia.get(0).getTitulos());
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
                if (lastSelectedRow < datos.size()) {
                    lsm.setSelectionInterval(firstSelectedRow, lastSelectedRow);
                } else {
                    lsm.setSelectionInterval(datos.size()-1, datos.size()-1);
                }

            }
            if (table.getColumnCount() > 1) {//TODO: CORREGIR ESTO PARA SIMPLIFICAR Y NO REPETIR CODIGO
                autoResizeColWidth(table);
            } else {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            }
//            return model;
        } else {
            Modelo<? extends ConvierteAVector> m;
            if (datos != null && !datos.isEmpty()) {
                m = new Modelo<T>(datos);
            } else {
                listaVacia = new ArrayList<ConvierteAVector>();
                listaVacia.add(datosNulos);
                m = new Modelo<ConvierteAVector>(listaVacia);
            }
            TableSorter tableSorter = new TableSorter(m, table.getTableHeader());
            table.setModel(tableSorter);
            if (table.getColumnCount() > 1) {
                autoResizeColWidth(table);
            } else {
                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            }
//            return tableSorter;
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
        if (datos != null && !datos.isEmpty()) {
            m = new Modelo<T>(datos);
        } else {
            listaVacia = new ArrayList<ConvierteAVector>();
            listaVacia.add(datosNulos);
            m = new Modelo<ConvierteAVector>(listaVacia);
        }
        table.setModel(m);
        if (table.getColumnCount() > 1) {
            autoResizeColWidth(table);
        } else {
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        }
//        return m;
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
        return getDatos().size();
    }

    @Override
    public int getColumnCount() {
        return getDatos().get(0).getTitulos().size();
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
        return getColumnas().get(col);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        try {
            Object dato = getValueAt(row, col);
            this.getDatos().get(row).setValueAt(col, dato);
            return true;
        } catch (UnsupportedOperationException e) {
            return false;
        }
    }

    public void setCellEditable(boolean editable) {
        this.cellEditable = editable;
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
        return this.columnas;
    }

    public final void setColumnas(List<String> columnas) {
        this.columnas = columnas;
    }

    public boolean isPrimeraVez() {
        return primeraVez;
    }

    public void setPrimeraVez(boolean primeraVez) {
        this.primeraVez = primeraVez;
    }

    public List<T> getTodos() {
        return todos;
    }

    public final void setTodos(List<T> todos) {
        this.todos = todos;
    }

    /**
     * @return the datos
     */
    public List<T> getDatos() {
        return datos;
    }

    /**
     * @param datos the datos to set
     */
    public final void setDatos(List<T> datos) {
        this.datos = datos;
    }
}
