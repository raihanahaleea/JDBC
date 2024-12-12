import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

// Parent class untuk transaksi
class Transaksi {
    protected String noFaktur;
    protected String kodeBarang;
    protected String namaBarang;
    protected double hargaBarang;
    protected int jumlahBeli;

    // Constructor
    public Transaksi(String noFaktur, String kodeBarang, String namaBarang, double hargaBarang, int jumlahBeli) {
        this.noFaktur = noFaktur;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.hargaBarang = hargaBarang;
        this.jumlahBeli = jumlahBeli;
    }

    // Method untuk menghitung total harga
    public double hitungTotal() {
        return hargaBarang * jumlahBeli;
    }
}

// Subclass untuk menangani validasi dan exception handling
class ValidasiTransaksi extends Transaksi {
    public ValidasiTransaksi(String noFaktur, String kodeBarang, String namaBarang, double hargaBarang, int jumlahBeli) {
        super(noFaktur, kodeBarang, namaBarang, hargaBarang, jumlahBeli);
    }

    // Method untuk memvalidasi data input
    public void validasiData() throws IllegalArgumentException {
        if (hargaBarang < 0) {
            throw new IllegalArgumentException("Harga barang tidak boleh negatif.");
        }
        if (jumlahBeli <= 0) {
            throw new IllegalArgumentException("Jumlah beli harus lebih dari 0.");
        }
    }
}

public class Main {
    // URL Database, Username, Password untuk PostgreSQL
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/seveneleven";
 // Ganti dengan URL DB Anda
    private static final String DB_USER = "postgres";  // Ganti dengan username PostgreSQL Anda
    private static final String DB_PASSWORD = "bismillahlulusgis"; // Ganti dengan password PostgreSQL Anda

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        // Login
        boolean loginBerhasil = false;
        while (!loginBerhasil) {
            System.out.println("+-----------------------------------------------------+");
            System.out.println("Log in ");
            System.out.print("Username (kasir): ");
            String username = scanner.nextLine();
            System.out.print("Password (kasir1): ");
            String password = scanner.nextLine();
            System.out.print("Captcha (vwxyz)  : ");
            String captcha = scanner.nextLine();

            if (username.equalsIgnoreCase("kasir") && password.equals("kasir1") && captcha.equalsIgnoreCase("VWXYZ")) {
                System.out.println("Login berhasil");
                loginBerhasil = true;
            } else {
                System.out.println("Login gagal, silakan coba lagi.");
            }
            System.out.println("+-----------------------------------------------------+");
        }

        // Header Supermarket
        System.out.println("Selamat Datang di Supermarket 7Eleven");
        Date now = new Date();
        System.out.println("Tanggal dan Waktu : " + dateFormat.format(now));

        // Koneksi ke database PostgreSQL
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            boolean exit = false;
            while (!exit) {
                System.out.println("\nMenu:");
                System.out.println("1. Tambah Transaksi (Create)");
                System.out.println("2. Tampilkan Transaksi (Read)");
                System.out.println("3. Perbarui Transaksi (Update)");
                System.out.println("4. Hapus Transaksi (Delete)");
                System.out.println("5. Keluar");
                System.out.print("Pilih menu: ");
                int pilihan = scanner.nextInt();
                scanner.nextLine(); // Konsumsi newline

                switch (pilihan) {
                    case 1:
                        // Create
                        System.out.print("Masukkan No Faktur      : ");
                        String noFaktur = scanner.nextLine();
                        System.out.print("Masukkan Kode Barang    : ");
                        String kodeBarang = scanner.nextLine();
                        System.out.print("Masukkan Nama Barang    : ");
                        String namaBarang = scanner.nextLine();
                        System.out.print("Masukkan Harga Barang   : ");
                        double hargaBarang = scanner.nextDouble();
                        System.out.print("Masukkan Jumlah Beli    : ");
                        int jumlahBeli = scanner.nextInt();

                        // Validasi Transaksi
                        ValidasiTransaksi transaksi = new ValidasiTransaksi(noFaktur, kodeBarang, namaBarang, hargaBarang, jumlahBeli);
                        transaksi.validasiData();

                        // Insert data transaksi ke database
                        String insertSQL = "INSERT INTO transaksi (noFaktur, kodeBarang, namaBarang, hargaBarang, jumlahBeli, totalHarga) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                            pstmt.setString(1, noFaktur);
                            pstmt.setString(2, kodeBarang);
                            pstmt.setString(3, namaBarang);
                            pstmt.setDouble(4, hargaBarang);
                            pstmt.setInt(5, jumlahBeli);
                            pstmt.setDouble(6, transaksi.hitungTotal());
                            pstmt.executeUpdate();
                            System.out.println("Transaksi berhasil ditambahkan.");
                        }
                        break;
                    case 2:
                        // Read
                        String selectSQL = "SELECT * FROM transaksi";
                        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
                            while (rs.next()) {
                                System.out.println("No Faktur   : " + rs.getString("noFaktur"));
                                System.out.println("Kode Barang : " + rs.getString("kodeBarang"));
                                System.out.println("Nama Barang : " + rs.getString("namaBarang"));
                                System.out.println("Harga Barang: " + rs.getDouble("hargaBarang"));
                                System.out.println("Jumlah Beli : " + rs.getInt("jumlahBeli"));
                                System.out.println("Total Harga : " + rs.getDouble("totalHarga"));
                                System.out.println("+----------------------------------+");
                            }
                        }
                        break;
                    case 3:
                        // Update
                        System.out.print("Masukkan No Faktur untuk diperbarui: ");
                        String fakturUpdate = scanner.nextLine();
                        System.out.print("Masukkan Harga Barang Baru: ");
                        double hargaBaru = scanner.nextDouble();
                        System.out.print("Masukkan Jumlah Beli Baru : ");
                        int jumlahBaru = scanner.nextInt();

                        String updateSQL = "UPDATE transaksi SET hargaBarang = ?, jumlahBeli = ?, totalHarga = ? WHERE noFaktur = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                            pstmt.setDouble(1, hargaBaru);
                            pstmt.setInt(2, jumlahBaru);
                            pstmt.setDouble(3, hargaBaru * jumlahBaru);
                            pstmt.setString(4, fakturUpdate);
                            pstmt.executeUpdate();
                            System.out.println("Transaksi berhasil diperbarui.");
                        }
                        break;
                    case 4:
                        // Delete
                        System.out.print("Masukkan No Faktur untuk dihapus: ");
                        String fakturDelete = scanner.nextLine();

                        String deleteSQL = "DELETE FROM transaksi WHERE noFaktur = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                            pstmt.setString(1, fakturDelete);
                            pstmt.executeUpdate();
                            System.out.println("Transaksi berhasil dihapus.");
                        }
                        break;
                    case 5:
                        exit = true;
                        break;
                    default:
                        System.out.println("Pilihan tidak valid.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Terjadi kesalahan: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
