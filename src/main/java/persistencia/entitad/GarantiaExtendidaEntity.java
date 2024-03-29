package persistencia.entitad;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;

@Entity(name = "GarantiaExtendida")
@NamedQuery(name = "GarantiaExtendida.findByCodigo", query = "SELECT garantia from GarantiaExtendida garantia where garantia.producto.codigo = :codigo")
public class GarantiaExtendidaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@OneToOne
	@JoinColumn(name = "ID_PRODUCTO", referencedColumnName = "id")
	private ProductoEntity producto;

	@Column(nullable = false)
	private Date fechaSolicitudGarantia;

	@Column(nullable = false)
	private Date fechaFinGarantia;

	@Column(nullable = false)
	private String nombreCliente;

	@Column(nullable = false)
	private double precio;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ProductoEntity getProducto() {
		return producto;
	}

	public void setProducto(ProductoEntity producto) {
		this.producto = producto;
	}

	public Date getFechaSolicitudGarantia() {
		return fechaSolicitudGarantia;
	}

	public void setFechaSolicitudGarantia(Date fechaSolicitudGarantia) {
		this.fechaSolicitudGarantia = fechaSolicitudGarantia;
	}

	public Date getFechaFinGarantia() {
		return fechaFinGarantia;
	}

	public void setFechaFinGarantia(Date fechaFinGarantia) {
		this.fechaFinGarantia = fechaFinGarantia;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		this.precio = precio;
	}

}
