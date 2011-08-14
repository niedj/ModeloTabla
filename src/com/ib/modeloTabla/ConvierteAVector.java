/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ib.modeloTabla;

import java.util.List;

/**
 *
 * @author Fede
 */
public interface ConvierteAVector{

    public List<String> getTitulos();

    public List<Object> getDatos();

    public void setValueAt(int posicion, Object value);
}
