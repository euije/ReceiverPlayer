import SwiftUI
import AVFoundation
import UniformTypeIdentifiers

struct ContentView: View {
    @State private var audioURL: URL? = nil
    @State private var player: AVAudioPlayer?
    @State private var isPlaying = false
    @State private var status = "🎧 파일을 선택해주세요"
    @State private var showingPicker = false

    var body: some View {
        VStack(spacing: 20) {
            Text(status)

            Button("🎼 오디오 파일 선택") {
                showingPicker = true
            }

            if let _ = audioURL {
                Button(isPlaying ? "⏸ 일시정지" : "▶️ 재생 (상단 스피커)") {
                    if isPlaying {
                        player?.pause()
                        isPlaying = false
                        status = "⏸ 일시정지"
                    } else {
                        prepareAudioSession()
                        do {
                            player = try AVAudioPlayer(contentsOf: audioURL!)
                            player?.prepareToPlay()
                            player?.play()
                            isPlaying = true
                            status = "▶️ 재생 중..."
                        } catch {
                            status = "❌ 재생 실패: \(error.localizedDescription)"
                        }
                    }
                }
            }
        }
        .padding()
        .sheet(isPresented: $showingPicker) {
            DocumentPickerView { url in
                audioURL = url
                status = "🎵 파일 선택됨"
                isPlaying = false
            }
        }
    }

    func prepareAudioSession() {
        let session = AVAudioSession.sharedInstance()
        do {
            try session.setCategory(.playAndRecord, mode: .voiceChat, options: [])
            try session.overrideOutputAudioPort(.none)
            try session.setActive(true)
        } catch {
            print("AudioSession 설정 실패: \(error)")
        }
    }
}

// MARK: - DocumentPickerView: SwiftUI용 UIDocumentPicker 래퍼
struct DocumentPickerView: UIViewControllerRepresentable {
    var onPick: (URL) -> Void

    func makeCoordinator() -> Coordinator {
        Coordinator(onPick: onPick)
    }

    func makeUIViewController(context: Context) -> UIDocumentPickerViewController {
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: [UTType.audio], asCopy: true)
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIDocumentPickerViewController, context: Context) {}

    class Coordinator: NSObject, UIDocumentPickerDelegate {
        let onPick: (URL) -> Void

        init(onPick: @escaping (URL) -> Void) {
            self.onPick = onPick
        }

        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            if let url = urls.first {
                onPick(url)
            }
        }
    }
}
