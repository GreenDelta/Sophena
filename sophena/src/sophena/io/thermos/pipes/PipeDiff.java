package sophena.io.thermos.pipes;

import sophena.model.HeatNetPipe;

/// Tracks the added length for a pipe during the sync. For new pipes, this is
/// the full length. For modified pipes, this is the positive length delta.
public record PipeDiff(HeatNetPipe pipe, double length) {
}
