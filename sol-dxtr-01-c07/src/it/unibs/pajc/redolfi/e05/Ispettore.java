package it.unibs.pajc.redolfi.e05;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * Esercizio 7.5: l'ispettore di finestre.
 *
 * Idea: a sinistra un JTree costruito ricorsivamente dai Container (ogni nodo porta
 * il componente come userObject); a destra una JTable il cui modello legge, per il
 * componente selezionato, i getter pubblici senza parametri (getX/isX, esclusi quelli
 * di una lista nera che bloccano o lanciano) e li chiama per reflection. Una
 * proprieta' e' modificabile se esiste un setter con lo stesso nome e il tipo del
 * getter, e il tipo e' String, int, boolean o Color: la stringa scritta nella cella si
 * converte nel tipo e si passa a invoke; un errore di conversione o di invoke finisce
 * nella riga di stato. Tutto gira sull'EDT, come ogni accesso a un componente.
 *
 * Errore tipico: passare la stringa al setter senza convertirla ("argument type
 * mismatch"), e chiamare i getter da un thread a parte.
 */
public class Ispettore {

    private static final Set<String> SALTA = Set.of("getGraphics", "getToolkit", "getTreeLock", "getPeer",
            "getParent", "getRootPane", "getTopLevelAncestor", "getFocusCycleRootAncestor", "getComponents",
            "getAccessibleContext", "getGraphicsConfiguration", "getInputContext", "getFocusTraversalKeys",
            "getListeners", "getUI", "getActionMap", "getInputMap", "getDropTarget", "getComponentListeners");

    private final JFrame frame = new JFrame("Ispettore");
    private final JFrame bersaglio;
    private final JTree albero = new JTree();
    private final ModelloProprieta proprieta = new ModelloProprieta();
    private final JLabel lblStato = new JLabel(" ");

    public Ispettore(JFrame bersaglio) {
        this.bersaglio = bersaglio;
        aggiorna();
        albero.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) albero.getLastSelectedPathComponent();
            proprieta.mostra(nodo == null ? null : (Component) nodo.getUserObject());
        });
        JTable tabella = new JTable(proprieta);
        JButton btnAggiorna = new JButton("Aggiorna");
        btnAggiorna.addActionListener(e -> aggiorna());
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(albero), new JScrollPane(tabella));
        split.setDividerLocation(300);
        frame.add(btnAggiorna, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.add(lblStato, BorderLayout.SOUTH);
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void aggiorna() {
        albero.setModel(new DefaultTreeModel(nodo(bersaglio)));
        for (int i = 0; i < albero.getRowCount(); i++)
            albero.expandRow(i);
    }

    private static DefaultMutableTreeNode nodo(Component c) {
        DefaultMutableTreeNode n = new DefaultMutableTreeNode(c) {
            @Override public String toString() {
                return c.getClass().getSimpleName() + (c.getName() != null ? " \"" + c.getName() + "\"" : "");
            }
        };
        if (c instanceof Container cont)
            for (Component figlio : cont.getComponents())
                n.add(nodo(figlio));
        return n;
    }

    /** Il modello della tabella: le proprieta' del componente selezionato. */
    private class ModelloProprieta extends AbstractTableModel {
        private record Proprieta(String nome, Method getter, Method setter) { }

        private Component componente;
        private final List<Proprieta> righe = new ArrayList<>();

        void mostra(Component c) {
            componente = c;
            righe.clear();
            if (c != null) {
                Class<?> k = c.getClass();
                for (Method m : k.getMethods()) {
                    String n = m.getName();
                    if (m.getParameterCount() != 0 || Modifier.isStatic(m.getModifiers()) || SALTA.contains(n)
                            || m.getReturnType() == void.class || m.getDeclaringClass() == Object.class)
                        continue;
                    String prop = n.startsWith("get") && n.length() > 3 ? n.substring(3)
                            : n.startsWith("is") && n.length() > 2 && m.getReturnType() == boolean.class ? n.substring(2) : null;
                    if (prop == null)
                        continue;
                    Method setter = null;
                    try {
                        setter = k.getMethod("set" + prop, m.getReturnType());
                    } catch (NoSuchMethodException ignorata) { }
                    righe.add(new Proprieta(Character.toLowerCase(prop.charAt(0)) + prop.substring(1), m, setter));
                }
                righe.sort(Comparator.comparing(Proprieta::nome));
            }
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return righe.size(); }
        @Override public int getColumnCount() { return 3; }
        @Override public String getColumnName(int c) { return new String[] { "proprieta'", "tipo", "valore" }[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Proprieta p = righe.get(r);
            if (c == 0) return p.nome() + (p.setter() != null && modificabile(p) ? " *" : "");
            if (c == 1) return p.getter().getReturnType().getSimpleName();
            try {
                Object v = p.getter().invoke(componente);
                return v instanceof Color col ? String.format("#%06X", col.getRGB() & 0xFFFFFF) : String.valueOf(v);
            } catch (Exception e) {
                return "(" + e.getClass().getSimpleName() + ")";
            }
        }

        private boolean modificabile(Proprieta p) {
            Class<?> t = p.getter().getReturnType();
            return t == String.class || t == int.class || t == boolean.class || t == Color.class;
        }

        @Override public boolean isCellEditable(int r, int c) { return c == 2 && righe.get(r).setter() != null && modificabile(righe.get(r)); }

        @Override
        public void setValueAt(Object valore, int r, int c) {
            Proprieta p = righe.get(r);
            Class<?> t = p.getter().getReturnType();
            String s = String.valueOf(valore).strip();
            try {
                Object v = t == String.class ? s : t == int.class ? Integer.valueOf(s)
                        : t == boolean.class ? Boolean.valueOf(s) : Color.decode(s);
                p.setter().invoke(componente, v);
                componente.repaint();
                lblStato.setText(p.nome() + " = " + s);
                fireTableRowsUpdated(r, r);
            } catch (Exception e) {
                lblStato.setText("impossibile impostare " + p.nome() + ": " + (e.getCause() != null ? e.getCause() : e));
            }
        }
    }

    /** Una finestra qualunque da ispezionare, e l'ispettore accanto. */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame f = new JFrame("Finestra di prova");
            JPanel barra = new JPanel();
            barra.setName("barra");
            barra.add(new JLabel("Nome:"));
            barra.add(new JTextField("Ada", 10));
            barra.add(new JButton("Saluta"));
            f.add(barra, BorderLayout.NORTH);
            JLabel centro = new JLabel("ciao", JLabel.CENTER);
            centro.setName("saluto");
            centro.setOpaque(true);
            centro.setBackground(new Color(0xE3F2FD));
            f.add(centro, BorderLayout.CENTER);
            f.setSize(400, 250);
            f.setLocation(100, 100);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setVisible(true);
            Ispettore isp = new Ispettore(f);
            isp.frame.setLocation(520, 100);
            isp.frame.setVisible(true);
        });
    }
}
