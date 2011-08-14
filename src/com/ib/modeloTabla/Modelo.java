package com.ib.modeloTabla;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

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

    public static <T extends ConvierteAVector>AbstractTableModel crearModelo(List<T> datos, JTable table) {
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
        return tableSorter;
    }

    public static <T extends ConvierteAVector>AbstractTableModel crearModeloSinSorter(List<T> datos, JTable table) {
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
        return m;
    }

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
        for (int columna = 0; columna < table.getColumnCount(); columna++) {
            TableColumn col = colModel.getColumn(columna);
            double width = 0;
            TableCellRenderer renderer = col.getHeaderRenderer();
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
            Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
            width = comp.getPreferredSize().getWidth();
            // Get maximum width of column data
            for (int r = 0; r < table.getRowCount(); r++) {
                renderer = table.getCellRenderer(r, columna);
                comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, columna), false, false, r, columna);
                width = Math.max(width, comp.getPreferredSize().getWidth());
            }
            width += 2 * margin;
            col.setPreferredWidth((int) width);
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

    public static void filtrar(String filtro, int col, AbstractTableModel modelo) {
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
                CharSequence filtros = filtro.subSequence(0, filtro.length());
                if (dato.contains(filtros)) {
                    m.getDatos().add(uno);
                }
            }
            m.fireTableDataChanged();
        }
    }

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
